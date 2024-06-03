package com.israel.planpilot

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtils {
    fun formatLocalDateToString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(formatter)
    }

    fun parseStringToLocalDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.parse(date, formatter)
    }
}
