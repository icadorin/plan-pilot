package com.israel.planpilot

import java.time.LocalDate

data class ActivityCard(
    var id: String = "",
    var activityId: String = "",
    var activityName: String = "",
    var alarmTriggerTime: String = "",
    var completed: Boolean = false,
    var date: String? = null
) {
    constructor() : this("", "", "", "", false)
}
