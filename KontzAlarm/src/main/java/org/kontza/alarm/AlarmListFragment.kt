package org.kontza.alarm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    var alarmEntryListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.e(LOG_TAG, "onDataChange")
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "loadItem:onCancelled", databaseError.toException())
            Toast.makeText(requireContext(), R.string.store_failed, Toast.LENGTH_SHORT).show()
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
                //get current item
                val currentItem = alarmsIterator.next()
                //get current data in a map
                val map = currentItem.value as HashMap<String, Any>
                //key will return Firebase ID
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
        alarmAdapter = AlarmAdapter(alarmList!!)
        alarmsListRecyclerView = _binding!!.recyclerView
        alarmsListRecyclerView!!.adapter = alarmAdapter
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}