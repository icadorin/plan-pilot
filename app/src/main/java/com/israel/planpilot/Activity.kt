package com.israel.planpilot

import java.time.LocalDateTime
import java.time.LocalTime


data class Activity(
    val name: String,
    val creationDateTime: LocalDateTime = LocalDateTime.now(),
    val activityTime: LocalTime? = null,
    val category: String? = null
) {
    init {
        require(name.isNotEmpty()) { "Name is required" }
    }

    fun toJson(): String {
        return """
            {
                "name": "$name",
                "creationDateTime": "$creationDateTime",
                "activityTime": "${activityTime ?: "Not specified"}",
                "category": "${category ?: "Not specified"}"
            }
        """.trimIndent()
    }
}


