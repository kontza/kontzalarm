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

class MainActivity : AppCompatActivity() {

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

        binding.fab.setOnClickListener { _ ->
            addNewAlarmDialog()
        }

        firebase = FirebaseDatabase.getInstance().reference
    }

    private fun addNewAlarmDialog() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

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
                        alert.setView(alarmEditText)
                        alert.setNegativeButton(
                            android.R.string.cancel,
                            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })
                        alert.setPositiveButton(android.R.string.ok) { _, _ ->
                            val pickedDateTime = Calendar.getInstance()
                            var alarmText = alarmEditText.text.toString()
                            pickedDateTime.set(year, month, day, hour, minute)
                            dateTimeSelected(pickedDateTime, alarmText)
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

    private fun dateTimeSelected(pickedDateTime: Calendar, alarmText: String) {
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
}