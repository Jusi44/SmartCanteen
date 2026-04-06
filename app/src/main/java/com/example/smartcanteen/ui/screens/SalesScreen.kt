package com.example.smartcanteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.smartcanteen.data.FoodItem
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.StatusColors
import kotlinx.coroutines.delay

@Composable
fun SalesScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All") + items.map { it.category }.distinct()
    val filteredItems = items.filter { (selectedCategory == "All" || it.category == selectedCategory) && it.name.contains(searchQuery, ignoreCase = true) }

    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var showSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Search for an item...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            if (items.isEmpty()) {
                EmptyState(Icons.Default.Storefront, "Add items to inventory first")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = selectedItem == item
                        val stockColor = StatusColors.getStockColor(item.currentStock, item.minThreshold)

                        Card(
                            onClick = { selectedItem = item }, 
                            modifier = Modifier.fillMaxWidth().animateItem(), 
                            shape = RoundedCornerShape(24.dp), 
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface), 
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) { 
                                    Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("₱${item.price}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Text(item.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(120.dp)) {
                                    Text(if (item.currentStock <= 0) "OUT OF STOCK" else "${item.currentStock} in stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = stockColor, textAlign = TextAlign.End)
                                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), shape = RoundedCornerShape(32.dp), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Order Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))) {
                                IconButton(onClick = { val c = quantity.toIntOrNull() ?: 1; if (c > 1) quantity = (c - 1).toString() }) { Icon(Icons.Default.Remove, null) }
                                Text(quantity, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 12.dp))
                                IconButton(onClick = { val c = quantity.toIntOrNull() ?: 1; quantity = (c + 1).toString() }) { Icon(Icons.Default.Add, null) }
                            }
                        }
                        val q = quantity.toIntOrNull() ?: 0
                        val can = selectedItem != null && q > 0 && selectedItem!!.currentStock >= q
                        val total = (selectedItem?.price ?: 0.0) * q
                        Button(
                            onClick = { 
                                selectedItem?.let { 
                                    viewModel.recordSale(it, q)
                                    selectedItem = null
                                    quantity = "1"
                                    showSuccess = true
                                } 
                            }, 
                            enabled = can, 
                            modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            val btnText = when {
                                selectedItem == null -> "SELECT AN ITEM"
                                q <= 0 -> "ENTER QUANTITY"
                                selectedItem!!.currentStock < q -> "STOCK INSUFFICIENT"
                                else -> "COMPLETE ORDER (₱$total)"
                            }
                            Text(btnText, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        
        // Success Overlay Animation
        if (showSuccess) {
            LaunchedEffect(Unit) { delay(1500); showSuccess = false }
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusColors.Green, modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sale Recorded!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
