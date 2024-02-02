package com.israel.planpilot

import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class ActivityRepository {
    private val gson = Gson()
    private var activities: MutableList<Activity> = loadActivities()

    fun createActivity(activity: Activity) {
        activities.add(activity)
        saveActivities()
    }

    fun readActivity(id: UUID): Activity? {
        return activities.find { it.id == id }
    }

    fun updateActivity(activity: Activity) {
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            activities[index] = activity
            saveActivities()
        }
    }

    fun deleteActivity(id: UUID) {
        activities.removeIf { it.id == id }
        saveActivities()
    }

    private fun saveActivities() {
        val jsonString = gson.toJson(activities)
        File("activities.json").writeText(jsonString)

        // Cópia dos dados p den. // ToDo remover
        val savePath = System.getenv("savePath")
        if (savePath != null) {
            File(savePath).writeText(jsonString)
        }
    }

    private fun loadActivities(): MutableList<Activity> {
        val file = File("activities.json")

        return if (file.exists()) {
            val jsonString = file.readText()
            gson.fromJson(jsonString, object : TypeToken<List<Activity>>() {}.type)
        } else {
            mutableListOf()
        }
    }
}


