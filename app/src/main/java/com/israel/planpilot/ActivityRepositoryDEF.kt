package com.israel.planpilot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ActivityRepositoryDEF(context: Context) {

    private val jsonFileName = "activities.json"
    private val jsonFile: File = File(context.filesDir, jsonFileName)

    suspend fun saveActivity(activityItem: ActivityItem, isEditOperation: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()

                if (isEditOperation) {
                    activities.removeAll { it.id == activityItem.id }
                }

                activities.add(activityItem)

                val json = Gson().toJson(activities)
                jsonFile.writeText(json)
            } catch (e: Exception) {
                println("Erro ao salvar atividade(s) no arquivo JSON: $e")
            }
        }
    }

    suspend fun deleteActivityById(activityId: String) {
        withContext(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()
                val updatedActivities = activities.filterNot { it.id == activityId }
                saveActivitiesToFile(updatedActivities)
            } catch (e: Exception) {
                println("Erro ao excluir atividade do arquivo JSON: $e")
            }
        }
    }

    fun getAllActivities(): List<ActivityItem> {
        return loadActivitiesFromFile()
    }

    fun loadActivitiesFromFile(): MutableList<ActivityItem> {
        return try {
            val json = jsonFile.readText()
            val typeToken = object : TypeToken<MutableList<ActivityItem>>() {}.type
            Gson().fromJson(json, typeToken) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveActivitiesToFile(activities: List<ActivityItem>) {
        try {
            val json = Gson().toJson(activities)
            jsonFile.writeText(json)
        } catch (e: Exception) {
            println("Erro ao salvar atividades no arquivo JSON: $e")
        }
    }
}
