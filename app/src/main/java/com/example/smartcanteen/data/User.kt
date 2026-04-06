package com.example.smartcanteen.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "STAFF" // ADMIN or STAFF
)
