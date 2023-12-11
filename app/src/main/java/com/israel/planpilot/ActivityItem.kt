package com.israel.planpilot

data class ActivityItem(
    var id: String = "",
    var name: String,
    val date: String,
    val time: String,
    var iconResource: Int
)