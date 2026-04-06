package com.example.smartcanteen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.data.FoodItem
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.components.ItemDialog
import com.example.smartcanteen.ui.components.RestockDialog
import com.example.smartcanteen.ui.components.StatusColors

@Composable
fun InventoryScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All") + items.map { it.category }.distinct()
    val filteredItems = items.filter { (selectedCategory == "All" || it.category == selectedCategory) && it.name.contains(searchQuery, ignoreCase = true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var restockingItem by remember { mutableStateOf<FoodItem?>(null) }
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Menu Item", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search menu...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category, 
                        onClick = { selectedCategory = category }, 
                        label = { Text(category, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (filteredItems.isEmpty()) {
                EmptyState(Icons.Default.Restaurant, "No items found matching your criteria")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredItems, key = { it.id }) { item ->
                        val stockProgress = if (item.currentStock > 0) (item.currentStock.toFloat() / (item.currentStock + 10).toFloat()).coerceIn(0f, 1f) else 0f
                        val stockColor = StatusColors.getStockColor(item.currentStock, item.minThreshold)

                        Card(
                            modifier = Modifier.fillMaxWidth().animateItem(), 
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                            Text("₱${item.price}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                                                Text(
                                                    item.category, 
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                                                    fontSize = 11.sp, 
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(100.dp)) {
                                        Text(if (item.currentStock <= 0) "EMPTY" else item.currentStock.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = stockColor, textAlign = TextAlign.End)
                                        Text("units left", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { stockProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = stockColor,
                                    trackColor = stockColor.copy(alpha = 0.15f)
                                )
                                
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { restockingItem = item }) { Icon(Icons.Default.AddCircle, null, tint = StatusColors.Green, modifier = Modifier.size(32.dp)) }
                                    IconButton(onClick = { editingItem = item }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(28.dp)) }
                                    IconButton(onClick = { itemToDelete = item }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp)) }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showAddDialog) ItemDialog(categories = items.map { it.category }.distinct(), onDismiss = { showAddDialog = false }, onConfirm = { n, c, p, s, t -> viewModel.addItem(n, c, p, s, t); showAddDialog = false })
    if (editingItem != null) ItemDialog(item = editingItem, categories = items.map { it.category }.distinct(), onDismiss = { editingItem = null }, onConfirm = { n, c, p, s, t -> viewModel.updateItem(editingItem!!.copy(name = n, category = c, price = p, currentStock = s, minThreshold = t)); editingItem = null })
    if (restockingItem != null) RestockDialog(item = restockingItem!!, onDismiss = { restockingItem = null }, onConfirm = { q -> viewModel.restockItem(restockingItem!!, q); restockingItem = null })
    if (itemToDelete != null) AlertDialog(onDismissRequest = { itemToDelete = null }, title = { Text("Delete Item", fontWeight = FontWeight.Bold) }, text = { Text("Are you sure you want to delete ${itemToDelete?.name}?") }, confirmButton = { Button(onClick = { itemToDelete?.let { viewModel.deleteItem(it) }; itemToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete", fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } })
}
