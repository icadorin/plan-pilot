package com.israel.planpilot

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ActivityRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val activitiesCollection = firestore.collection("activities")

    suspend fun createActivity(activity: Activity): String {
        val id = activity.id.toString()
        activitiesCollection.document(id).set(activity).await()
        return activity.id
    }

    suspend fun readActivity(id: String): Activity? {
        return withContext(Dispatchers.IO) {
            var activity: Activity? = null
            try {
                activity = activitiesCollection
                    .document(id)
                    .get()
                    .await()
                    .toObject(Activity::class.java)
            } catch (e: Exception) {
                println("Erro ao ler a atividade: ${e.message}")
            }
            activity
        }
    }

    suspend fun updateActivity(activity: Activity) {
        activitiesCollection.document(activity.id.toString()).set(activity).await()
    }

    suspend fun deleteActivity(id: String) {
        activitiesCollection.document(id).delete().await()
    }
}
