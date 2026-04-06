package com.example.smartcanteen.data

import com.google.firebase.firestore.DocumentId

data class StockRecord(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val quantityChanged: Int = 0,
    val type: String = "", // RESTOCK or SALE
    val userId: String = "",
    val username: String = "System",
    val timestamp: Long = System.currentTimeMillis()
)
