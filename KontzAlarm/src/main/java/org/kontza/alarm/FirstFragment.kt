package org.kontza.alarm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import org.kontza.alarm.Constants.LOG_TAG
import org.kontza.alarm.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var firebase: DatabaseReference
    private var alarmList: MutableList<AlarmItem>? = null
    private lateinit var alarmAdapter: AlarmAdapter
    private var alarmsListView: ListView? = null
    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
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
        //alert adapter that has changed
        alarmAdapter.notifyDataSetChanged()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmList = mutableListOf<AlarmItem>()
        alarmAdapter = AlarmAdapter(requireContext(), alarmList!!)
        alarmsListView = _binding!!.alarmsList
        alarmsListView!!.adapter = alarmAdapter
        firebase = FirebaseDatabase.getInstance().reference
        firebase.orderByKey().addListenerForSingleValueEvent(itemListener)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}