package com.israel.planpilot

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class ActivityRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val activitiesCollection = firestore.collection("activities")

    suspend fun createActivity(activity: Activity): String {
        val id = activity.id
        activitiesCollection.document(id).set(activity).await()
        return activity.id
    }

    fun readAllActivities(onSuccess: (List<Activity>) -> Unit) {
        activitiesCollection.addSnapshotListener { value, error ->
            if (error != null) {
                println("Erro ao ler as atividades: ${(error as FirebaseFirestoreException).message}")
                return@addSnapshotListener
            }

            val activities = value?.toObjects(Activity::class.java) ?: listOf()
            onSuccess(activities)
        }
    }

    suspend fun updateActivity(activity: Activity) {
        activitiesCollection.document(activity.id).set(activity).await()
    }

    suspend fun deleteActivity(id: String) {
        activitiesCollection.document(id).delete().await()
    }
}
