package com.israel.planpilot.model

import java.util.UUID

data class ActivityCardModel(
    val id: String = UUID.randomUUID().toString(),
    var activityId: String = "",
    var activityName: String = "",
    var alarmTriggerTime: String = "",
    var completed: Boolean? = null,
    var date: String? = null
)
