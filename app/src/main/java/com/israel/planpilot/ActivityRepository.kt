package com.israel.planpilot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

class ActivityRepository(private val context: Context) {
    private val gson = Gson()
    private var activities: MutableList<Activity> = loadActivities()

    fun createActivity(activity: Activity): UUID {
        activities.add(activity)
        saveActivities()

        return activity.id
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val internalFilesDir = context.filesDir
                val file = File(internalFilesDir, "activities.json")
                val jsonString = gson.toJson(activities)
                file.writeText(jsonString)
                println("Dados salvos com sucesso!")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun loadActivities(): MutableList<Activity> {
        val internalFilesDir = context.filesDir
        val file = File(internalFilesDir, "activities.json")

        return if (file.exists()) {
            val jsonString = file.readText()
            gson.fromJson(jsonString, object : TypeToken<List<Activity>>() {}.type)
        } else {
            mutableListOf()
        }
    }
}
