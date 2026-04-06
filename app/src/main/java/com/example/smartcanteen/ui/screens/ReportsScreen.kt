package com.example.smartcanteen.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.StatusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: CanteenViewModel, navController: NavHostController) {
    val sales by viewModel.allSales.collectAsState()
    val items by viewModel.allItems.collectAsState()
    val dateSdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val context = LocalContext.current
    
    var timeFilter by remember { mutableStateOf("All Time") }
    val filteredSales = remember(sales, timeFilter) {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        when (timeFilter) {
            "Today" -> sales.filter { now - it.timestamp < dayMillis }
            "7 Days" -> sales.filter { now - it.timestamp < 7 * dayMillis }
            else -> sales
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sales Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { 
                    val reportText = "SmartCanteen Sales Report\n" + 
                        "Total Revenue: ₱${String.format("%.2f", filteredSales.sumOf { it.totalPrice })}\n" +
                        "Total Sales: ${filteredSales.size}\n\n" +
                        filteredSales.joinToString("\n") { "${dateSdf.format(Date(it.timestamp))} - ${it.itemName}: ₱${it.totalPrice}" }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                }) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { navController.navigate("stock_history") }, 
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) { 
                    Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(20.dp)) 
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Today", "7 Days", "All Time")) { filter ->
                FilterChip(
                    selected = timeFilter == filter,
                    onClick = { timeFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), 
            shape = RoundedCornerShape(32.dp), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text("Revenue (${timeFilter})", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("₱${String.format("%.2f", filteredSales.sumOf { it.totalPrice })}", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text("${filteredSales.size} transactions", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Text("Transaction Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        if (filteredSales.isEmpty()) {
            EmptyState(Icons.Outlined.ReceiptLong, "No transactions for this period")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredSales, key = { it.id }) { sale ->
                    val relatedItem = items.find { it.id == sale.itemId }
                    val stockColor = if (relatedItem != null) {
                        StatusColors.getStockColor(relatedItem.currentStock, relatedItem.minThreshold)
                    } else StatusColors.Green

                    Card(modifier = Modifier.fillMaxWidth().animateItem(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = { 
                                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.secondary) }
                                }
                            },
                            headlineContent = { Text(sale.itemName, fontWeight = FontWeight.ExtraBold) },
                            supportingContent = { Text(dateSdf.format(Date(sale.timestamp)), style = MaterialTheme.typography.labelSmall) },
                            trailingContent = { 
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(100.dp)) {
                                    Text("+₱${sale.totalPrice}", fontWeight = FontWeight.Black, color = stockColor, fontSize = 16.sp, textAlign = TextAlign.End)
                                    Text("${sale.quantitySold} units", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
                                }
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}
