package com.example.smartcanteen.data

import com.google.firebase.firestore.DocumentId

data class FoodItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val currentStock: Int = 0,
    val minThreshold: Int = 5
)

data class Sale(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemName: String = "",
    val quantitySold: Int = 0,
    val totalPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)

data class StockRecord(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val quantityChanged: Int = 0,
    val type: String = "", // RESTOCK or SALE
    val userId: String = "",
    val username: String = "System",
    val timestamp: Long = System.currentTimeMillis()
)

data class User(
    @DocumentId val id: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "STAFF" // ADMIN or STAFF
)
