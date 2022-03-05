package org.kontza.alarm

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AlarmItem(
    val objectId: String? = null,
    val utcAlarmTime: Long? = null,
    val alarmText: String? = null,
) {
}