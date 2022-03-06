package org.kontza.alarm

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import org.kontza.alarm.Constants.LOG_TAG
import org.kontza.alarm.databinding.FragmentAlarmListBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AlarmListFragment : Fragment() {

    private var _binding: FragmentAlarmListBinding? = null
    private lateinit var firebase: DatabaseReference
    private var alarmList: MutableList<AlarmItem>? = null
    private lateinit var alarmAdapter: AlarmAdapter
    private var alarmsListRecyclerView: RecyclerView? = null
    private var swipeBackground: ColorDrawable = ColorDrawable(Color.RED)
    private lateinit var deleteIcon: Drawable
    private lateinit var listener: OnAlarmItemSelected

    private val itemTouchHelperCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val alarm = alarmAdapter.getItem(viewHolder.adapterPosition)
                firebase.child(Constants.FIREBASE_ALARMS).child(alarm.objectId!!).removeValue()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                if (dX > 0) {
                    swipeBackground.setBounds(
                        itemView.left,
                        itemView.top,
                        dX.toInt(),
                        itemView.bottom
                    )
                    deleteIcon.setBounds(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMargin
                    )
                } else {
                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deleteIcon.setBounds(
                        itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin
                    )
                }
                swipeBackground.draw(c)
                c.save()
                if (dX > 0) {
                    c.clipRect(
                        itemView.left,
                        itemView.top,
                        dX.toInt(),
                        itemView.bottom
                    )
                } else {
                    c.clipRect(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                }
                deleteIcon.draw(c)
                c.restore()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
    private val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)

    var alarmEntryListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "loadItem:onCancelled", databaseError.toException())
            Toast.makeText(requireContext(), R.string.store_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAlarmItemSelected) {
            listener = context
        } else {
            throw ClassCastException(
                "$context must implement OnAlarmItemSelected."
            )
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        alarmList!!.clear()
        val items = dataSnapshot.children.iterator()
        // Check if current database contains any collection
        Log.i(Constants.LOG_TAG, "Items? ${items.hasNext()}")
        if (items.hasNext()) {
            val alarmIndex = items.next()
            val alarmsIterator = alarmIndex.children.iterator()

            //check if the collection has any to do items or not
            while (alarmsIterator.hasNext()) {
                val currentItem = alarmsIterator.next()
                val map = currentItem.value as HashMap<String, Any>
                val alarmItem = AlarmItem(
                    currentItem.key,
                    map["utcAlarmTime"] as Long?,
                    map["alarmText"] as String?
                )
                Log.i(LOG_TAG, "Alarm item: $alarmItem")
                alarmList!!.add(alarmItem)
            }
        }
        alarmList!!.sortBy { i -> i.utcAlarmTime }
        alarmAdapter.notifyDataSetChanged()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmListBinding.inflate(inflater, container, false)
        firebase = FirebaseDatabase.getInstance().reference
        firebase.orderByKey().addValueEventListener(alarmEntryListener)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmList = mutableListOf<AlarmItem>()
        alarmAdapter = AlarmAdapter(alarmList!!) { item ->
            listener.onAlarmItemSelected(item)
        }
        alarmsListRecyclerView = _binding!!.recyclerView
        alarmsListRecyclerView!!.adapter = alarmAdapter
        itemTouchHelper.attachToRecyclerView(alarmsListRecyclerView!!)
        deleteIcon =
            ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)!!
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnAlarmItemSelected {
        fun onAlarmItemSelected(alarmItem: AlarmItem)
    }

}