package com.israel.planpilot.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
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
}

