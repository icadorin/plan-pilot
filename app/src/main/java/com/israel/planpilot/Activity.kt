package com.israel.planpilot

import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Activity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val day: Int?,
    val month: Int?,
    val year: Int?,
    val time: String,
    val creationDateTime: LocalDateTime = LocalDateTime.now(),
    val contactForMessage: Int? = null,
    val alarmTriggerTime: LocalTime? = null,
    val category: String? = null
) {
    init {
        require(name.isNotEmpty()) { "Name is required" }
    }

    fun toJson(): String {
        return """
            {
                "id": "$id",
                "name": "$name",
                "day": "$day",
                "month": "$month",
                "year": "$year",
                "time": "$time",
                "creationDateTime": "$creationDateTime",
                "contactForMessage": "$contactForMessage"
                "activityTime": "${alarmTriggerTime ?: "Not specified"}",
                "category": "${category ?: "Not specified"}"
            }
        """.trimIndent()
    }
}
