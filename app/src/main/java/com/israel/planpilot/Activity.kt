package com.israel.planpilot

import android.net.Uri
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Activity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val day: Int?,
    val month: Int?,
    val year: Int?,
    val creationDateTime: LocalDateTime = LocalDateTime.now(),
    val contactForMessage: Int? = null,
    val alarmTriggerTime: LocalTime? = null,
    val alarmActivated: Boolean = false,
    val alarmTone: Uri? = null,
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
                "creationDateTime": "$creationDateTime",
                "contactForMessage": "$contactForMessage",
                "activityTime": "${alarmTriggerTime ?: "Not specified"}",
                "alarmActivated": "$alarmActivated",
                "alarmTone": "$alarmTone",
                "category": "${category ?: "Not specified"}"
            }
        """.trimIndent()
    }
}
