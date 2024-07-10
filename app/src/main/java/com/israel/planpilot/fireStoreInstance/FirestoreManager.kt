package com.israel.planpilot.fireStoreInstance

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val instance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getFirestoreInstance(): FirebaseFirestore {
        return instance
    }
}
