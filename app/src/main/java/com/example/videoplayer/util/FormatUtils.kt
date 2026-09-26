package com.example.videoplayer.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

fun formatDateHeader(epochSec: Long): String {
    val cal = Calendar.getInstance()
    val today = cal.clone() as Calendar
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val itemCal = Calendar.getInstance().apply { timeInMillis = epochSec * 1000 }
    itemCal.set(Calendar.HOUR_OF_DAY, 0)
    itemCal.set(Calendar.MINUTE, 0)
    itemCal.set(Calendar.SECOND, 0)
    itemCal.set(Calendar.MILLISECOND, 0)

    val diffDays = ((today.timeInMillis - itemCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
    return when (diffDays) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(epochSec * 1000))
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(epochSec * 1000))
    }
}
