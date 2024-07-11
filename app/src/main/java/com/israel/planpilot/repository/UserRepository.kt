package com.israel.planpilot.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.israel.planpilot.fireStoreInstance.FirestoreManager

import com.israel.planpilot.model.UserModel

class UserRepository {

    private val usersCollection = FirestoreManager.getFirestoreInstance().collection("users")
    private val tag = "UserRepository"

    fun addUser(user: UserModel, userId: String): Task<Void> {
        val userRef = usersCollection.document(userId)
        return userRef.set(user)
            .addOnSuccessListener {
                Log.d(tag, "Usuário adicionado com ID: $userId")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao adicionar usuário", e)
            }
    }

    fun getUser(userId: String): Task<DocumentSnapshot> {
        val userRef = usersCollection.document(userId)
        return userRef.get()
    }

    fun updateUser(userId: String, updatedUser: Map<String, Any>): Task<Void> {
        val userRef = usersCollection.document(userId)
        return userRef.update(updatedUser)
            .addOnSuccessListener {
                Log.d(tag, "Usuário atualizado com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao atualizar usuário", e)
            }
    }

    fun deleteUser(userId: String): Task<Void> {
        val userRef = usersCollection.document(userId)
        return userRef.delete()
            .addOnSuccessListener {
                Log.d(tag, "Usuário deletado com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao deletar usuário", e)
            }
    }

    fun addActivityToUser(userId: String, activityRef: CollectionReference) {
        val userRef = usersCollection.document(userId)
        userRef.update("activities", FieldValue.arrayUnion(activityRef))
            .addOnSuccessListener {
                Log.d(tag, "Atividade adicionada ao usuário com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao adicionar atividade ao usuário", e)
            }
    }

    fun removeActivityFromUser(userId: String, activityRef: CollectionReference) {
        val userRef = usersCollection.document(userId)
        userRef.update("activities", FieldValue.arrayRemove(activityRef))
            .addOnSuccessListener {
                Log.d(tag, "Atividade removida do usuário com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao remover atividade do usuário", e)
            }
    }

    fun addActivityCardToUser(userId: String, activityCardRef: CollectionReference) {
        val userRef = usersCollection.document(userId)
        userRef.update("activityCards", FieldValue.arrayUnion(activityCardRef))
            .addOnSuccessListener {
                Log.d(tag, "Card de atividade adicionado ao usuário com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao adicionar card de atividade ao usuário", e)
            }
    }

    fun removeActivityCardFromUser(userId: String, activityCardRef: CollectionReference) {
        val userRef = usersCollection.document(userId)
        userRef.update("activityCards", FieldValue.arrayRemove(activityCardRef))
            .addOnSuccessListener {
                Log.d(tag, "Card de atividade removido do usuário com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Erro ao remover card de atividade do usuário", e)
            }
    }
}
