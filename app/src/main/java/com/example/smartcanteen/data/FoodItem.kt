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
