package com.israel.planpilot.repository

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestoreException
import com.israel.planpilot.fireStoreInstance.FirestoreManager
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.model.ActivityModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ActivityRepository {
    private val firestore = FirestoreManager.getFirestoreInstance()
    private val activityCardRepository = ActivityCardRepository()
    private val activitiesCollection = firestore.collection("activities")
    private var activitiesCache: List<ActivityModel>? = null

    suspend fun createActivity(activity: ActivityModel): String {
        val id = activity.id
        activitiesCollection.document(id).set(activity).await()
        activitiesCache = null
        return activity.id
    }

    fun readAllActivities(onSuccess: (List<ActivityModel>) -> Unit) {
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
                        val activity = change.document.toObject(ActivityModel::class.java)
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

    fun readTodayActivities(onSuccess: (List<ActivityModel>) -> Unit) {
        val currentDate = LocalDate.now().toString()

        activitiesCollection
            .whereGreaterThanOrEqualTo("startDate", currentDate)
            .whereLessThanOrEqualTo("endDate", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val activities = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(ActivityModel::class.java)?.apply {
                        startDate = LocalDate.parse(startDate).toString()
                        endDate = LocalDate.parse(endDate).toString()
                    }
                }.take(3)

                onSuccess(activities)
            }
            .addOnFailureListener { exception ->
                println("Erro ao ler as atividades: ${exception.message}")
            }
    }

    suspend fun readAllActivityCards(): List<ActivityCardModel> {
        return activityCardRepository.getAllActivityCards()
    }

    suspend fun getAllActivities(): List<ActivityModel> {
        return if (activitiesCache != null) {
            activitiesCache!!
        } else {
            val querySnapshot = activitiesCollection.get().await()
            val activities = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(ActivityModel::class.java)?.apply {
                    startDate = LocalDate.parse(startDate).toString()
                    endDate = LocalDate.parse(endDate).toString()
                }
            }
            activitiesCache = activities
            activities
        }
    }

    suspend fun updateActivity(activity: ActivityModel) {
        activitiesCollection.document(activity.id).set(activity).await()
        activitiesCache = null
    }

    suspend fun deleteActivity(id: String) {
        val activityCardRepository = ActivityCardRepository()
        activityCardRepository.deleteActivityCardsByActivityId(id)
        activitiesCollection.document(id).delete().await()
        activitiesCache = null
    }

    suspend fun getActivityById(activityId: String): ActivityModel? {
        return try {
            val documentSnapshot = activitiesCollection.document(activityId).get().await()
            documentSnapshot.toObject(ActivityModel::class.java)
        } catch (e: Exception) {
            println("Erro ao obter atividade pelo ID: ${e.message}")
            null
        }
    }

    suspend fun getActivityCardByActivityId(activityId: String): ActivityCardModel? {
        val querySnapshot = activitiesCollection.whereEqualTo("activityId", activityId).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCardModel::class.java)
        } else {
            null
        }
    }
}
