package com.israel.planpilot

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ActivityRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val activitiesCollection = firestore.collection("activities")
    private var activitiesCache: List<Activity>? = null

    suspend fun createActivity(activity: Activity): String {
        val id = activity.id
        activitiesCollection.document(id).set(activity).await()
        activitiesCache = null
        return activity.id
    }

    fun readAllActivities(onSuccess: (List<Activity>) -> Unit) {
        activitiesCache?.let { cachedActivities ->
            onSuccess(cachedActivities)
            return
        }

        activitiesCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                println("Erro ao ler as atividades: ${(error as FirebaseFirestoreException).message}")
                return@addSnapshotListener
            }

            val updatedActivities = snapshots?.documentChanges?.mapNotNull { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val activity = change.document.toObject(Activity::class.java)
                        activity.apply {
                            startDate = LocalDate.parse(startDate).toString()
                            endDate = LocalDate.parse(endDate).toString()
                        }
                    }
                    DocumentChange.Type.REMOVED -> null
                }
            } ?: listOf()

            activitiesCache = updatedActivities
            onSuccess(updatedActivities)
        }
    }

    suspend fun updateActivity(activity: Activity) {
        activitiesCollection.document(activity.id).set(activity).await()
        activitiesCache = null
    }

    suspend fun deleteActivity(id: String) {
        activitiesCollection.document(id).delete().await()
        activitiesCache = null
    }

    suspend fun getActivityById(activityId: String): Activity? {
        return try {
            val documentSnapshot = activitiesCollection.document(activityId).get().await()
            documentSnapshot.toObject(Activity::class.java)
        } catch (e: Exception) {
            println("Erro ao obter atividade pelo ID: ${e.message}")
            null
        }
    }
}
