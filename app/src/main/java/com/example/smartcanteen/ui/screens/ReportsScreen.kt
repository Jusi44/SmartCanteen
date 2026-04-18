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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.StatusColors
import com.example.smartcanteen.ui.components.EmptyState
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

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Reports",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
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
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).shadow(2.dp, CircleShape)
                ) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { navController.navigate("stock_history") },
                    modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape).shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Today", "7 Days", "All Time")) { filter ->
                val isSelected = timeFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { timeFilter = filter },
                    label = { Text(filter, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).shadow(12.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, Color(0xFFE65100))
                )
            )) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("NET REVENUE", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                    Text("₱${String.format("%.2f", filteredSales.sumOf { it.totalPrice })}", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${filteredSales.size} Transactions", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Text("Recent Transactions", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge, letterSpacing = (-0.5).sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (filteredSales.isEmpty()) {
            EmptyState(Icons.Outlined.ReceiptLong, "No transactions found for $timeFilter")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredSales.reversed(), key = { it.id }) { sale ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) {
                                    Box(contentAlignment = Alignment.Center) { 
                                        Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) 
                                    }
                                }
                            },
                            headlineContent = { Text(sale.itemName, fontWeight = FontWeight.Black, fontSize = 16.sp) },
                            supportingContent = { Text(dateSdf.format(Date(sale.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("+₱${sale.totalPrice}", fontWeight = FontWeight.Black, color = StatusColors.Green, fontSize = 16.sp)
                                    Text("${sale.quantitySold} units", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
