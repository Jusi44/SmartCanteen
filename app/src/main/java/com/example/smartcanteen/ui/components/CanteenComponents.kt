package com.example.smartcanteen.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object StatusColors {
    val Red = Color(0xFFD32F2F)    // Vibrant Red
    val Orange = Color(0xFFEF6C00) // Deep Orange (Ensures it doesn't look yellow)
    val Green = Color(0xFF2E7D32)  // Vibrant Green
    
    val RedText = Color(0xFFB71C1C) 
    val OrangeText = Color(0xFFE65100)
    val GreenText = Color(0xFF1B5E20)
    
    val RedBg = Color(0xFFFFEBEE)
    val OrangeBg = Color(0xFFFFF3E0)
    val GreenBg = Color(0xFFE8F5E9)

    @Composable
    fun getStockColor(stock: Int, threshold: Int): Color {
        return when {
            stock <= threshold -> Red    // Red if it is "Low" (at or below threshold)
            stock <= threshold + 5 -> Orange // Orange if it is "Close to Low" (within 5 units)
            else -> Green // Green if it is "Not Low"
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, textColor: Color = Color.Unspecified) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = color), 
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.5f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
