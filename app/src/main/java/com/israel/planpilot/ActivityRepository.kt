package com.israel.planpilot

import android.app.Activity

class ActivityRepository {
    private val activities = mutableListOf<Activity>()

    fun createActivity(activity: Activity) {
        activities.add(activity)
    }

//    fun readActivity(name: String): Activity? {
//        return activities.find { it.name == name }
//    }
//
//    fun updateActivity(activity: Activity) {
//        val index = activities.indexOfFirst { it.name == activity.name }
//        if (index != -1) {
//            activities[index] = activity
//        }
//    }
//
//    fun deleteActivity(name: String) {
//        activities.removeIf { it.name == name }
//    }
}
