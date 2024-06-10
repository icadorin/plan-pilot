package com.israel.planpilot

class ActivityCard(
    var id: String = "",
    var activityId: String = "",
    var activityName: String = "",
    var date: String = "",
    var isCompleted: Boolean = false
) {
    constructor() : this("", "", "", "", false)
}
