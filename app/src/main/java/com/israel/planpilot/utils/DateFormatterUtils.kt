package com.israel.planpilot.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormatterUtils {
    fun formatLocalDateToString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(formatter)
    }

    fun formatDateToDefault(date: String): String {
        val parsedDate = LocalDate.parse(date)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return parsedDate.format(formatter)
    }
}
