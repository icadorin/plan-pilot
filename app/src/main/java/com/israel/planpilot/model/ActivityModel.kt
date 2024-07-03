package com.israel.planpilot.model

import java.util.UUID

data class ActivityModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val day: Int? = null,
    val month: Int? = null,
    val year: Int? = null,
    val time: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    val contactForMessage: Int? = null,
    val alarmTriggerTime: String? = null,
    val alarmActivated: Boolean = false,
    val alarmTone: String? = null,
    val category: String? = null,
    val weekDays: List<String>? = null
)
