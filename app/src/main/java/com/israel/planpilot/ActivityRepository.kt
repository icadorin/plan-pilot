package com.israel.planpilot

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

        activitiesCollection.addSnapshotListener { value, error ->
            if (error != null) {
                println("Erro ao ler as atividades: ${(error as FirebaseFirestoreException).message}")
                return@addSnapshotListener
            }

            val activities = value?.toObjects(Activity::class.java)?.onEach { activity ->
                activity.startDate = LocalDate.parse(activity.startDate).toString()
                activity.endDate = LocalDate.parse(activity.endDate).toString()
            } ?: listOf()

            activitiesCache = activities
            onSuccess(activities)
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
}
