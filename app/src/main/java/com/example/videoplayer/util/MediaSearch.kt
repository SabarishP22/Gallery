package com.example.videoplayer.util

import com.example.videoplayer.data.GalleryMedia
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val monthAliases = mapOf(
    "jan" to 1, "january" to 1,
    "feb" to 2, "february" to 2,
    "mar" to 3, "march" to 3,
    "apr" to 4, "april" to 4,
    "may" to 5,
    "jun" to 6, "june" to 6,
    "jul" to 7, "july" to 7,
    "aug" to 8, "augest" to 8, "august" to 8,
    "sep" to 9, "sept" to 9, "september" to 9,
    "oct" to 10, "october" to 10,
    "nov" to 11, "november" to 11,
    "dec" to 12, "december" to 12,
)

fun mediaMatchesQuery(media: GalleryMedia, rawQuery: String): Boolean {
    val query = rawQuery.trim()
    if (query.isEmpty()) return true

    val qLower = query.lowercase(Locale.getDefault())
    if (media.displayName.lowercase(Locale.getDefault()).contains(qLower)) return true

    val cal = Calendar.getInstance().apply { timeInMillis = media.dateAddedSec * 1000 }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)

    val datePatterns = listOf(
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()),
        SimpleDateFormat("MMMM d", Locale.getDefault()),
        SimpleDateFormat("MMM d", Locale.getDefault()),
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("EEEE", Locale.getDefault()),
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy", Locale.getDefault()),
    ).map { it.format(Date(media.dateAddedSec * 1000)).lowercase(Locale.getDefault()) }

    if (datePatterns.any { it.contains(qLower) }) return true

    val tokens = qLower.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.all { token -> tokenMatches(token, year, month, day, datePatterns) }) return true

    return false
}

private fun tokenMatches(
    token: String,
    year: Int,
    month: Int,
    day: Int,
    datePatterns: List<String>,
): Boolean {
    if (datePatterns.any { it.contains(token) }) return true

    token.toIntOrNull()?.let { number ->
        if (number in 1900..2100 && number == year) return true
        if (number in 1..31 && number == day) return true
    }

    monthAliases[token]?.let { if (it == month) return true }

    val monthDay = Regex("^(\\d{1,2})[/-](\\d{1,2})$").find(token)
    if (monthDay != null) {
        val m = monthDay.groupValues[1].toIntOrNull()
        val d = monthDay.groupValues[2].toIntOrNull()
        if (m == month && d == day) return true
    }

    val dayMonth = Regex("^(\\d{1,2})[/-](\\d{1,2})$").find(token)
    if (dayMonth != null) {
        val d = dayMonth.groupValues[1].toIntOrNull()
        val m = dayMonth.groupValues[2].toIntOrNull()
        if (d == day && m == month) return true
    }

    return false
}
