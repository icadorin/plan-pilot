package com.israel.planpilot

import java.util.UUID
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Activity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val day: Int?,
    val month: Int?,
    val year: Int?,
    val time: String? = null,
    val contactForMessage: Int? = null,
    @SerializedName("alarm_trigger_time")
    val alarmTriggerTime: String? = null,
    @SerializedName("alarm_activated")
    val alarmActivated: Boolean = false,
    @SerializedName("alarm_tone")
    val alarmTone: String? = null,
    val category: String? = null
) {
    init {
        require(name.isNotEmpty()) { "Name is required" }
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

