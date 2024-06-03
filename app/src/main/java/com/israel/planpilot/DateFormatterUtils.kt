package com.israel.planpilot

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtils {

    fun stringToDateFormatted(data: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(data)
    }

    fun convertToLocalDate(date: String): LocalDate {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        val localDate = LocalDate.parse(date, inputFormatter)
        val formattedDate = localDate.format(outputFormatter)

        return LocalDate.parse(formattedDate, outputFormatter)
    }
}