package com.israel.planpilot.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateFormatterUtils {
    fun formatLocalDateToString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(formatter)
    }

    fun formatDateToDefault(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate: Date? = try {
            inputFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
        return parsedDate?.let { outputFormat.format(it) } ?: ""
    }
}
