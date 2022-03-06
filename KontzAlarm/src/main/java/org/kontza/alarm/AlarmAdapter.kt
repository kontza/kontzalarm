package org.kontza.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*

class AlarmAdapter(
    private val alarmList: MutableList<AlarmItem>,
    private val listener: (AlarmItem) -> Unit
) :
    RecyclerView.Adapter<AlarmAdapter.ListRowHolder>() {

    class ListRowHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById<TextView>(R.id.alarm_description)
        val time: TextView = view.findViewById<TextView>(R.id.alarm_time)
        var objectId: String = ""
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListRowHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.alarm_item, viewGroup, false)
        val holder = ListRowHolder(view)
        holder.itemView.setOnClickListener {
            listener(alarmList[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ListRowHolder, position: Int) {
        holder.objectId = alarmList[position].objectId as String
        holder.description.text = alarmList[position].alarmText
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarmList[position].utcAlarmTime!!
        holder.time.text = "${DateFormat.getDateInstance().format(calendar.time)} ${
            DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
        }"
    }

    override fun getItemCount() = alarmList.size
    override fun getItemId(index: Int): Long = index.toLong()
    fun getItem(position: Int): AlarmItem = alarmList[position]
}