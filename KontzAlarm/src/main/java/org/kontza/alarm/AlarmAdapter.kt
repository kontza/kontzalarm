package org.kontza.alarm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.kontza.alarm.Constants.LOG_TAG
import java.text.DateFormat
import java.util.*

class AlarmAdapter(context: Context, alarmList: MutableList<AlarmItem>) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var alarmList = alarmList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.i(LOG_TAG, "getView item ${alarmList[position]}")
        val alarmText: String = alarmList[position].alarmText as String
        val utcAlarmTime = alarmList[position].utcAlarmTime as Long
        val view: View
        val vh: ListRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.alarm_items, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.description.text = alarmText
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = utcAlarmTime
        vh.time.text = "${DateFormat.getDateInstance().format(calendar.time)} ${
            DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
        }"
        return view
    }

    override fun getItem(index: Int): Any {
        return alarmList.get(index)
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getCount(): Int {
        return alarmList.size
    }

    private class ListRowHolder(row: View?) {
        val description: TextView = row!!.findViewById<TextView>(R.id.alarm_description) as TextView
        val time: TextView = row!!.findViewById<TextView>(R.id.alarm_time) as TextView
    }
}