package com.tobibur.journey.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun formatTimestamp(timeMillis: Long): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy h:mm a")
        val zoneId = ZoneId.systemDefault()
        return Instant.ofEpochMilli(timeMillis).atZone(zoneId).format(formatter)
    } else {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy h:mm a", Locale.getDefault())
        return formatter.format(Date(timeMillis))
    }
}
