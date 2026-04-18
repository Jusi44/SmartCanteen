package com.example.smartcanteen.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object StatusColors {
    val Red = Color(0xFFD32F2F)
    val Orange = Color(0xFFEF6C00)
    val Green = Color(0xFF2E7D32)

    val RedText = Color(0xFFB71C1C)
    val OrangeText = Color(0xFFE65100)
    val GreenText = Color(0xFF1B5E20)

    val RedBg = Color(0xFFFFEBEE)
    val OrangeBg = Color(0xFFFFF3E0)
    val GreenBg = Color(0xFFE8F5E9)

    @Composable
    fun getStockColor(stock: Int, threshold: Int): Color {
        return when {
            stock <= threshold -> Red
            stock <= threshold + 5 -> Orange
            else -> Green
        }
    }
}
