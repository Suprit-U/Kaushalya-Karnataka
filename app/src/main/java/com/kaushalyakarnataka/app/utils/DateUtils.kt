package com.kaushalyakarnataka.app.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    /**
     * Formats a Firebase Timestamp to a standard readable string (e.g., "12 Oct, 2024").
     */
    fun formatTimestamp(timestamp: Timestamp, format: String = "dd MMM, yyyy"): String {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Formats a Timestamp into a relative time string (e.g., "Just now", "2 hrs ago", "Yesterday").
     * Useful for reviews and notifications.
     */
    fun getRelativeTimeSpan(timestamp: Timestamp): String {
        val now = Date().time
        val time = timestamp.toDate().time
        val diff = now - time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> formatTimestamp(timestamp)
        }
    }
}
