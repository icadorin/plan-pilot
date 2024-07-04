package com.israel.planpilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.israel.planpilot.model.ActivityCardModel
import kotlinx.coroutines.tasks.await

class ActivityCardRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val collectionRef = db.collection("activityCards")
    private var activityCardsCache: MutableList<ActivityCardModel> = mutableListOf()

    suspend fun initializeCache() {
        fetchActivityCards()
    }

    private suspend fun fetchActivityCards() {
        val querySnapshot = collectionRef.get().await()
        activityCardsCache = querySnapshot.documents.mapNotNull {
            it.toObject(ActivityCardModel::class.java)
        }.toMutableList()
    }

    suspend fun addActivityCard(activityCardModel: ActivityCardModel) {
        try {
            val documentRef = collectionRef.document(activityCardModel.id)
            documentRef.set(activityCardModel).await()
            activityCardsCache.add(activityCardModel)
        } catch (e: Exception) {
            throw Exception("Erro ao adicionar o cartão de atividade: ${e.message}")
        }
    }

    fun getUncompletedActivityCards(): List<ActivityCardModel> {
        return activityCardsCache.filter { it.completed == null }
    }

    suspend fun getAllActivityCards(): List<ActivityCardModel> {
        val querySnapshot = collectionRef.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject(ActivityCardModel::class.java) }
    }

    suspend fun findActivityCardByActivityAndDate(activityId: String, date: String): ActivityCardModel? {
        val querySnapshot = collectionRef
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("date", date)
            .get()
            .await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCardModel::class.java)
        } else {
            null
        }
    }

    suspend fun updateActivityCardCompletion(activityCardId: String, completed: Boolean) {
        try {
            val cardRef = collectionRef.document(activityCardId)

            cardRef.update("completed", completed).await()

            val updatedCardSnapshot = cardRef.get().await()
            val updatedCard = updatedCardSnapshot.toObject(ActivityCardModel::class.java)

            updatedCard?.let {
                val index = activityCardsCache.indexOfFirst { card -> card.id == it.id }
                if (index != -1) {
                    activityCardsCache[index] = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erro ao atualizar documento no Firestore: ${e.message}")
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
