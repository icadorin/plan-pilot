package com.israel.planpilot.model

data class ActivityCardModel(
    var id: String = "",
    var activityId: String = "",
    var activityName: String = "",
    var alarmTriggerTime: String = "",
    var completed: Boolean? = null,
    var date: String? = null
) {
    constructor() : this("", "", "", "", null, null)
}
