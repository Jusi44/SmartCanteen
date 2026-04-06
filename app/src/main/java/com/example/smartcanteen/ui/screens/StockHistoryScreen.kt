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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.StatusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockHistoryScreen(viewModel: CanteenViewModel) {
    val records by viewModel.allStockRecords.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Audit Trail", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
        Text("Detailed Movement History", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (records.isEmpty()) {
            EmptyState(Icons.Default.History, "No activity recorded")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(records, key = { it.id }) { record ->
                    val itemName = allItems.find { it.id == record.itemId }?.name ?: "Removed Item"
                    val isRestock = record.type == "RESTOCK"
                    
                    val accentColor = if (isRestock) StatusColors.Green else StatusColors.Red
                    val bgColor = if (isRestock) StatusColors.GreenBg else StatusColors.RedBg
                    val icon = if (isRestock) Icons.Default.Inventory else Icons.Default.ShoppingCartCheckout
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(), 
                        shape = RoundedCornerShape(16.dp), 
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            // Left Accent Bar - Restore color indicator
                            Box(modifier = Modifier.fillMaxHeight().width(8.dp).background(accentColor))
                            
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.6f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(28.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(itemName, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.Black)
                                    Text(sdf.format(Date(record.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                                    Text(
                                        "${if (isRestock) "RESTOCK" else "SALE"} recorded by ${record.username}", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = accentColor, 
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                Text(
                                    if (isRestock) "+${record.quantityChanged}" else "-${record.quantityChanged}",
                                    fontWeight = FontWeight.Black, 
                                    color = accentColor, 
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
