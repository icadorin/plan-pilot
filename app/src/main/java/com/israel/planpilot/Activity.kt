package com.israel.planpilot

import java.util.UUID

data class Activity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val day: Int?,
    val month: Int?,
    val year: Int?,
    val time: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val contactForMessage: Int? = null,
    val alarmTriggerTime: String? = null,
    val alarmActivated: Boolean = false,
    val alarmTone: String? = null,
    val category: String? = null,
    val weekDays: List<String>? = null
) {
    init {
        require(name.isNotEmpty()) { "Name is required" }
    }
}
