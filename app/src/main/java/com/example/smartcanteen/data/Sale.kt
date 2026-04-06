package com.example.smartcanteen.data

import com.google.firebase.firestore.DocumentId

data class Sale(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemName: String = "",
    val quantitySold: Int = 0,
    val totalPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)
