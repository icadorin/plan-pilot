package com.israel.planpilot.repository

import com.israel.planpilot.fireStoreInstance.FirestoreManager
import com.israel.planpilot.model.ActivityCardModel
import kotlinx.coroutines.tasks.await

class ActivityCardRepository {
    private val firestore = FirestoreManager.getFirestoreInstance()
    private val collectionRef = firestore.collection("activityCards")
    private val activityCardsCacheMap = mutableMapOf<String, MutableList<ActivityCardModel>>()
    private var isCacheInitialized = false

    suspend fun initializeCache(userId: String) {
        if (!isCacheInitialized) {
            fetchActivityCards(userId)
            isCacheInitialized = true
        }
    }

    private suspend fun fetchActivityCards(userId: String) {
        try {
            val querySnapshot = collectionRef.whereEqualTo("createdBy", userId).get().await()
            val activityCards = querySnapshot.documents.mapNotNull {
                it.toObject(ActivityCardModel::class.java)
            }.toMutableList()
            activityCardsCacheMap[userId] = activityCards
            println("Cards resgatados com sucesso")
        } catch (e: Exception) {
            println("Erro ao buscar activityCards do Firestore: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchUncompletedActivityCardsFromFirestore(userId: String): List<ActivityCardModel> {
        return try {
            val querySnapshot = collectionRef
                .whereEqualTo("createdBy", userId)
                .whereEqualTo("completed", null) // Filtrando apenas os cards não completados
                .get()
                .await()

            querySnapshot.documents.mapNotNull {
                it.toObject(ActivityCardModel::class.java)
            }
        } catch (e: Exception) {
            println("Erro ao buscar activityCards do Firestore: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addActivityCard(activityCardModel: ActivityCardModel, userId: String) {
        try {
            activityCardModel.createdBy = userId
            val documentRef = collectionRef.document(activityCardModel.id)
            documentRef.set(activityCardModel).await()
            activityCardsCacheMap[userId]?.add(activityCardModel)
        } catch (e: Exception) {
            throw Exception("Erro ao adicionar o cartão de atividade: ${e.message}")
        }
    }

    fun getUncompletedActivityCards(userId: String): List<ActivityCardModel> {
        return activityCardsCacheMap[userId]?.filter { it.completed == null } ?: listOf()
    }

    fun getAllActivityCards(userId: String): List<ActivityCardModel> {
        return activityCardsCacheMap[userId] ?: listOf()
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

    suspend fun updateActivityCardCompletion(activityCardId: String, completed: Boolean, userId: String) {
        try {
            val cardRef = collectionRef.document(activityCardId)
            val cardSnapshot = cardRef.get().await()
            val card = cardSnapshot.toObject(ActivityCardModel::class.java)

            if (card?.createdBy == userId) {
                cardRef.update("completed", completed).await()

                val updatedCardSnapshot = cardRef.get().await()
                val updatedCard = updatedCardSnapshot.toObject(ActivityCardModel::class.java)

                updatedCard?.let {
                    val cache = activityCardsCacheMap[userId]
                    val index = cache?.indexOfFirst { it.id == updatedCard.id }
                    if (index != null && index != -1) {
                        cache[index] = updatedCard
                    }
                }
            } else {
                println("Usuário não tem permissão para atualizar este card de atividade.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erro ao atualizar documento no Firestore: ${e.message}")
        }
    }

    suspend fun deleteActivityCardsByActivityId(activityId: String, userId: String) {
        try {
            val querySnapshot = collectionRef
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("createdBy", userId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                collectionRef.document(document.id).delete().await()
                activityCardsCacheMap[userId]?.removeIf { it.id == document.id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erro ao excluir cards de atividade do Firestore: ${e.message}")
        }
    }

    fun clearUserCache(userId: String) {
        activityCardsCacheMap.remove(userId)
    }
}
