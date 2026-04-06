package com.example.smartcanteen.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.StatCard
import com.example.smartcanteen.ui.components.StatusColors
import java.util.*

@Composable
fun DashboardScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val lowStockItems = items.filter { it.currentStock <= it.minThreshold }
    
    val topItems = sales.groupBy { it.itemName }
        .mapValues { entry -> entry.value.sumOf { it.quantitySold } }
        .toList().sortedByDescending { it.second }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Dashboard Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Items", items.size.toString(), Icons.Outlined.Inventory2, MaterialTheme.colorScheme.primaryContainer)
                StatCard(Modifier.weight(1f), "Alerts", lowStockItems.size.toString(), Icons.Outlined.Notifications, if (lowStockItems.isNotEmpty()) StatusColors.RedBg else MaterialTheme.colorScheme.secondaryContainer, if (lowStockItems.isNotEmpty()) StatusColors.RedText else MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        item {
            Text("Sales Trend & Daily Top Sellers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            AnimatedSalesChart(sales)
        }

        item {
            Text("All-Time Top Performers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        if (topItems.isEmpty()) {
            item { EmptyState(Icons.Default.History, "No sales recorded yet") }
        } else {
            items(topItems.size) { index ->
                val (name, qty) = topItems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("#${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("$qty sold", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }

        if (lowStockItems.isNotEmpty()) {
            item {
                Text("Low Stock Alerts", color = StatusColors.RedText, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            }
            items(lowStockItems) { item ->
                val alertMessage = if (item.currentStock == 0) "OUT OF STOCK" else "Only ${item.currentStock} left"
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    colors = CardDefaults.cardColors(containerColor = StatusColors.RedBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (item.currentStock == 0) Icons.Default.Block else Icons.Default.ErrorOutline, null, tint = StatusColors.RedText)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.name, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.weight(1f), fontSize = 16.sp)
                        Text(alertMessage, fontSize = 14.sp, color = StatusColors.RedText, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun AnimatedSalesChart(sales: List<com.example.smartcanteen.data.Sale>) {
    val hasRealData = sales.isNotEmpty()
    
    // Data preparation: Revenue and Top Product per day
    val chartData = remember(sales) {
        val dataList = mutableListOf<Pair<Double, String>>()
        if (!hasRealData) {
            // Mock data
            val mockNames = listOf("Burger", "Coke", "Pizza", "Fries", "Rice", "Pasta", "Drink")
            val mockRevenue = listOf(30.0, 50.0, 40.0, 80.0, 60.0, 95.0, 70.0)
            mockRevenue.zip(mockNames).forEach { dataList.add(it) }
        } else {
            for (i in 0..6) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                
                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + (24 * 60 * 60 * 1000)
                
                val daySales = sales.filter { it.timestamp in dayStart until dayEnd }
                val totalForDay = daySales.sumOf { it.totalPrice }
                val topProduct = daySales.groupBy { it.itemName }
                    .maxByOrNull { entry -> entry.value.sumOf { s -> s.quantitySold } }?.key ?: ""
                
                dataList.add(Pair(totalForDay, topProduct))
            }
            dataList.reverse()
        }
        dataList
    }
    
    val maxVal = (chartData.map { it.first }.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(180.dp), // Increased height slightly
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            chartData.forEach { (revenue, topProduct) ->
                val heightPercent by animateFloatAsState(
                    targetValue = (revenue / maxVal).toFloat().coerceIn(0.1f, 1f),
                    animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "chart"
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Item Name Label (Rotated to fit better)
                    if (topProduct.isNotEmpty()) {
                        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = topProduct,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.rotate(-45f) // Rotate text to fit
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(heightPercent)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                ),
                                alpha = if (hasRealData) 1f else 0.3f
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("6 days ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
