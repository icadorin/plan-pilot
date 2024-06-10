package com.israel.planpilot

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ActivityCardRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val collectionRef = db.collection("activityCards")

    suspend fun addActivityCard(activityCard: ActivityCard) {
        collectionRef.add(activityCard).await()
    }

    suspend fun getActivityCardById(id: Int): ActivityCard? {
        val querySnapshot = collectionRef.whereEqualTo("id", id).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCard::class.java)
        } else {
            null
        }
    }

    suspend fun getAllActivityCards(): List<ActivityCard> {
        val querySnapshot = collectionRef.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject(ActivityCard::class.java) }
    }

    suspend fun updateActivityCard(activityCard: ActivityCard) {
        val querySnapshot = collectionRef.whereEqualTo("id", activityCard.id).get().await()
        if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents[0].id
            collectionRef.document(documentId).set(activityCard).await()
        }
    }

    suspend fun deleteActivityCard(id: Int) {
        val querySnapshot = collectionRef.whereEqualTo("id", id).get().await()
        if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents[0].id
            collectionRef.document(documentId).delete().await()
        }
    }

    suspend fun getActivityCardByActivityId(activityId: String): ActivityCard? {
        val querySnapshot = collectionRef.whereEqualTo("activityId", activityId).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject(ActivityCard::class.java)
        } else {
            null
        }
    }
}
