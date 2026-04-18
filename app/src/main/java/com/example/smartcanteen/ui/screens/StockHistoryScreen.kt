package com.example.smartcanteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.StatusColors
import com.example.smartcanteen.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockHistoryScreen(viewModel: CanteenViewModel) {
    val records by viewModel.allStockRecords.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Text(
            "Audit Trail",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )
        Text(
            "Detailed stock movement history",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (records.isEmpty()) {
            EmptyState(Icons.Default.History, "No activity recorded yet")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(records.reversed(), key = { it.id }) { record ->
                    val itemName = allItems.find { it.id == record.itemId }?.name ?: "Removed Item"
                    val isRestock = record.type == "RESTOCK"

                    val accentColor = if (isRestock) StatusColors.Green else StatusColors.Red
                    val bgColor = if (isRestock) StatusColors.GreenBg else StatusColors.RedBg
                    val icon = if (isRestock) Icons.Default.Inventory else Icons.Default.ShoppingCartCheckout

                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = bgColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(26.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    itemName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${if (isRestock) "Added to stock" else "Sold"} by ${record.username}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    sdf.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (isRestock) "+${record.quantityChanged}" else "-${record.quantityChanged}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Black,
                                    color = accentColor,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
