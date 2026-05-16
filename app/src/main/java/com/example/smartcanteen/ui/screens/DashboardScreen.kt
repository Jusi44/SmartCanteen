package com.example.smartcanteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.StatusColors
import com.example.smartcanteen.ui.components.AnimatedSalesChart
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.StatCard
import com.example.smartcanteen.ui.theme.GradientEnd
import com.example.smartcanteen.ui.theme.GradientStart

@Composable
fun DashboardScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val lowStockItems = items.filter { it.currentStock <= it.minThreshold }

    val topItems = sales.groupBy { it.itemName }
        .mapValues { entry -> entry.value.sumOf { it.quantitySold } }
        .toList().sortedByDescending { it.second }.take(3)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Overview of your canteen's performance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        Modifier.weight(1f),
                        "Total Menu",
                        items.size.toString(),
                        Icons.Outlined.Inventory2,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatCard(
                        Modifier.weight(1f),
                        "Low Stock",
                        lowStockItems.size.toString(),
                        Icons.Outlined.Notifications,
                        if (lowStockItems.isNotEmpty()) StatusColors.RedBg else MaterialTheme.colorScheme.secondaryContainer,
                        if (lowStockItems.isNotEmpty()) StatusColors.RedText else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Sales Trend",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        letterSpacing = (-0.5).sp
                    )
                    Surface(
                        modifier = Modifier.shadow(12.dp, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White
                    ) {
                        AnimatedSalesChart(sales)
                    }
                }
            }

            if (lowStockItems.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusColors.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Attention Required",
                            color = StatusColors.RedText,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                items(lowStockItems) { item ->
                    val isOutOfStock = item.currentStock == 0
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isOutOfStock) StatusColors.RedBg.copy(alpha = 0.5f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (isOutOfStock) StatusColors.Red else StatusColors.Orange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isOutOfStock) Icons.Default.Block else Icons.Default.PriorityHigh,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    if (isOutOfStock) "Out of Stock" else "Only ${item.currentStock} left in inventory",
                                    fontSize = 12.sp,
                                    color = if (isOutOfStock) StatusColors.RedText else StatusColors.OrangeText
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Top Sellers",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    letterSpacing = (-0.5).sp
                )
            }

            if (topItems.isEmpty()) {
                item { EmptyState(Icons.Default.History, "No sales data available yet") }
            } else {
                items(topItems.size) { index ->
                    val (name, qty) = topItems[index]
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(GradientStart, GradientEnd)
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            }
                            Surface(
                                color = GradientStart.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "$qty sold",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = GradientEnd,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
