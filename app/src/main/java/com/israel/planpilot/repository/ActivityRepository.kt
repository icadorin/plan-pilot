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

    suspend fun createActivity(activity: ActivityModel, createdBy: String): String {
        activity.createdBy = createdBy
        val id = activity.id
        activitiesCollection.document(id).set(activity).await()
        activitiesCache = null
        return activity.id
    }

    fun readAllActivities(userId: String, onSuccess: (List<ActivityModel>) -> Unit) {
        activitiesCache?.let { cachedActivities ->
            onSuccess(cachedActivities.filter { it.createdBy == userId })
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
            onSuccess(updatedActivities.filter { it.createdBy == userId })
        }
    }

    fun readTodayActivities(userId: String, onSuccess: (List<ActivityModel>) -> Unit) {
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
                }.filter { it.createdBy == userId }.take(3)

                onSuccess(activities)
            }
            .addOnFailureListener { exception ->
                println("Erro ao ler as atividades: ${exception.message}")
            }
    }

    suspend fun getAllActivities(userId: String): List<ActivityModel> {
        return if (activitiesCache != null) {
            activitiesCache!!.filter { it.createdBy == userId }
        } else {
            val querySnapshot = activitiesCollection.get().await()
            val activities = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(ActivityModel::class.java)?.apply {
                    startDate = LocalDate.parse(startDate).toString()
                    endDate = LocalDate.parse(endDate).toString()
                }
            }.filter { it.createdBy == userId }

            activitiesCache = activities
            activities
        }
    }

    suspend fun updateActivity(activity: ActivityModel, userId: String) {
        activity.createdBy = userId
        activitiesCollection.document(activity.id).set(activity).await()
        activitiesCache = null
    }

    suspend fun deleteActivity(id: String, userId: String) {
        try {
            activityCardRepository.deleteActivityCardsByActivityId(id, userId)
            activitiesCollection.document(id).delete().await()
            activitiesCache = null
        } catch (e: Exception) {
            println("Erro ao excluir atividade: ${e.message}")
            throw e
        }
    }

    suspend fun getActivityById(activityId: String, userId: String): ActivityModel? {
        return try {
            val documentSnapshot = activitiesCollection.document(activityId).get().await()
            val activity = documentSnapshot.toObject(ActivityModel::class.java)
            if (activity?.createdBy == userId) {
                activity.apply {
                    startDate = LocalDate.parse(startDate).toString()
                    endDate = LocalDate.parse(endDate).toString()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Erro ao obter atividade pelo ID: ${e.message}")
            null
        }
    }

    suspend fun getActivityCardByActivityId(activityId: String, userId: String): ActivityCardModel? {
        val querySnapshot = activitiesCollection.whereEqualTo("activityId", activityId).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            val activity = querySnapshot.documents[0].toObject(ActivityModel::class.java)
            if (activity?.createdBy == userId) {
                activityCardRepository.findActivityCardByActivityAndDate(activityId, LocalDate.now().toString())
            } else {
                null
            }
        } else {
            null
        }
    }
}
