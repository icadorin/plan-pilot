package com.israel.planpilot.model

import com.google.firebase.firestore.CollectionReference
import java.util.UUID

data class UserModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = "",
    val activities: CollectionReference? = null,
    val activityCards: CollectionReference? = null
)