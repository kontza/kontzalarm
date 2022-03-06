package org.kontza.alarm

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.kontza.alarm.Constants.FIREBASE_ALARMS
import org.kontza.alarm.Constants.LOG_TAG
import org.kontza.alarm.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), AlarmListFragment.OnAlarmItemSelected {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.fab.setOnClickListener {
            showAlarmEditDialog(null)
        }
        firebase = FirebaseDatabase.getInstance().reference
    }

    private fun showAlarmEditDialog(alarmItem: AlarmItem?) {
        val currentDateTime = Calendar.getInstance()
        var startYear = currentDateTime.get(Calendar.YEAR)
        var startMonth = currentDateTime.get(Calendar.MONTH)
        var startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        var startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        var startMinute = currentDateTime.get(Calendar.MINUTE)
        if (alarmItem != null) {
            currentDateTime.timeInMillis = alarmItem.utcAlarmTime!!
            startYear = currentDateTime.get(Calendar.YEAR)
            startMonth = currentDateTime.get(Calendar.MONTH)
            startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
            startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
            startMinute = currentDateTime.get(Calendar.MINUTE)
        }

        DatePickerDialog(
            this@MainActivity,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                TimePickerDialog(
                    this@MainActivity,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        val alert = AlertDialog.Builder(this@MainActivity)
                        val alarmEditText = EditText(this@MainActivity)
                        alert.setMessage(R.string.alarm_text)
                        alert.setTitle(R.string.enter_alarm_text)
                        if (alarmItem != null) {
                            alarmEditText.setText(alarmItem.alarmText)
                        }
                        alert.setView(alarmEditText)
                        alert.setNegativeButton(
                            android.R.string.cancel,
                            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })
                        alert.setPositiveButton(android.R.string.ok) { _, _ ->
                            val pickedDateTime = Calendar.getInstance()
                            var alarmText = alarmEditText.text.toString()
                            pickedDateTime.set(year, month, day, hour, minute)
                            if (alarmItem == null) {
                                saveAlarm(pickedDateTime, alarmText)
                            } else {
                                saveAlarm(
                                    AlarmItem(
                                        alarmItem.objectId,
                                        pickedDateTime.timeInMillis,
                                        alarmText
                                    )
                                )
                            }
                        }
                        alert.show()
                    },
                    startHour,
                    startMinute,
                    is24HourFormat(this@MainActivity)
                ).show()
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }

    private fun saveAlarm(alarmItem: AlarmItem) {
        val oldAlarm = firebase.child(FIREBASE_ALARMS).child(alarmItem.objectId as String)
        Log.i(LOG_TAG, "oldAlarm = $alarmItem")
        oldAlarm.setValue(alarmItem).addOnSuccessListener {
            Log.i(LOG_TAG, getString(R.string.alarm_stored))
            Toast.makeText(this@MainActivity, R.string.alarm_updated, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e(LOG_TAG, "${getString(R.string.store_failed)}: $exception")
            Toast.makeText(this@MainActivity, R.string.store_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveAlarm(pickedDateTime: Calendar, alarmText: String) {
        val newAlarm = firebase.child(FIREBASE_ALARMS).push()
        val alarmItem = AlarmItem(newAlarm.key, pickedDateTime.timeInMillis, alarmText)
        Log.i(LOG_TAG, "newAlarm = $alarmItem")
        newAlarm.setValue(alarmItem).addOnSuccessListener {
            Log.i(LOG_TAG, getString(R.string.alarm_stored))
            Toast.makeText(this@MainActivity, R.string.alarm_stored, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e(LOG_TAG, "${getString(R.string.store_failed)}: $exception")
            Toast.makeText(this@MainActivity, R.string.store_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onAlarmItemSelected(alarmItem: AlarmItem) {
        Log.e(LOG_TAG, "Item '$alarmItem' clicked.")
        showAlarmEditDialog(alarmItem)
    }
}