package com.israel.planpilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.israel.planpilot.model.ActivityCardModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ActivityCardRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val collectionRef = db.collection("activityCards")
    private var activityCardsCache: MutableList<ActivityCardModel> = mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchActivityCards()
        }
    }

    private suspend fun fetchActivityCards() {
        val querySnapshot = collectionRef.get().await()
        activityCardsCache = querySnapshot.documents.mapNotNull {
            it.toObject(ActivityCardModel::class.java)
        }.toMutableList()
    }

    fun getAllActivityCardsFromCache(): List<ActivityCardModel> {
        return activityCardsCache.toList()
    }

    suspend fun addActivityCard(activityCardModel: ActivityCardModel) {
        collectionRef.add(activityCardModel).await()
        activityCardsCache.add(activityCardModel)
    }

    suspend fun getActivityCardById(id: Int): ActivityCardModel? {
        val querySnapshot = collectionRef.whereEqualTo("id", id).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCardModel::class.java)
        } else {
            null
        }
    }

    suspend fun getAllActivityCards(): List<ActivityCardModel> {
        val querySnapshot = collectionRef.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject(ActivityCardModel::class.java) }
    }

    suspend fun updateActivityCard(activityCardModel: ActivityCardModel) {
        val querySnapshot = collectionRef.whereEqualTo("id", activityCardModel.id).get().await()
        if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents[0].id
            collectionRef.document(documentId).set(activityCardModel).await()
            val index = activityCardsCache.indexOfFirst { it.id == activityCardModel.id }
            if (index != -1) {
                activityCardsCache[index] = activityCardModel
            }
        }
    }

    suspend fun updateStatusActivityCard(activityCardModel: ActivityCardModel, completed: Boolean) {
        val querySnapshot = collectionRef.whereEqualTo("id", activityCardModel.id).get().await()
        if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents[0].id
            collectionRef.document(documentId).update("completed", completed).await()
            val index = activityCardsCache.indexOfFirst { it.id == activityCardModel.id }
            if (index != -1) {
                activityCardsCache[index].completed = completed
            }
        }
    }

    suspend fun getActiveActivityCardsWithNullCompletion(): List<ActivityCardModel> {
        val querySnapshot = collectionRef.whereEqualTo("completed", null)
            .get().await()
        return querySnapshot.documents.mapNotNull { it.toObject(ActivityCardModel::class.java) }
    }

    suspend fun deleteActivityCard(id: String) {
        val querySnapshot = collectionRef.whereEqualTo("id", id).get().await()
        if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents[0].id
            collectionRef.document(documentId).delete().await()
            activityCardsCache.removeIf { it.id == id }
        }
    }

    suspend fun deleteActivityCardsForDate(date: LocalDate) {
        val querySnapshot = collectionRef.whereEqualTo("date", date.toString()).get().await()
        for (document in querySnapshot.documents) {
            collectionRef.document(document.id).delete().await()
        }
    }

    suspend fun getActivityCardByActivityId(activityId: String): ActivityCardModel? {
        val querySnapshot = collectionRef.whereEqualTo("activityId", activityId).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCardModel::class.java)
        } else {
            null
        }
    }

    suspend fun findActivityCardByActivityAndDate(activityId: String, date: String): ActivityCardModel? {
        val querySnapshot = collectionRef.whereEqualTo("activityId", activityId).whereEqualTo("date", date).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCardModel::class.java)
        } else {
            null
        }
    }

    suspend fun deleteActivityCardsByActivityId(activityId: String) {
        val querySnapshot = collectionRef.whereEqualTo("activityId", activityId).get().await()
        for (document in querySnapshot.documents) {
            collectionRef.document(document.id).delete().await()
            activityCardsCache.removeIf { it.id == document.id }
        }
    }
}
