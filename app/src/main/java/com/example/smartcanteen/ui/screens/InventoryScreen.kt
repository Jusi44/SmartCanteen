package com.example.smartcanteen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.data.FoodItem
import com.example.smartcanteen.ui.StatusColors
import com.example.smartcanteen.ui.components.EmptyState

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
                text = { Text("Add Item", fontWeight = FontWeight.Black) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
            Text(
                "Inventory",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search items...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        border = if (!isSelected) FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = MaterialTheme.colorScheme.outline) else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (filteredItems.isEmpty()) {
                EmptyState(Icons.Default.Restaurant, "No items found in this category")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val stockProgress = if (item.currentStock > 0) (item.currentStock.toFloat() / (item.currentStock + 15).toFloat()).coerceIn(0.1f, 1f) else 0f
                        val stockColor = StatusColors.getStockColor(item.currentStock, item.minThreshold)

                        Surface(
                            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Black, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("₱${item.price}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    item.category,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            if (item.currentStock <= 0) "EMPTY" else item.currentStock.toString(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp,
                                            color = stockColor,
                                            letterSpacing = (-1).sp
                                        )
                                        Text("IN STOCK", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                
                                LinearProgressIndicator(
                                    progress = { stockProgress },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                    color = stockColor,
                                    trackColor = stockColor.copy(alpha = 0.1f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { restockingItem = item },
                                        modifier = Modifier.background(StatusColors.GreenBg, CircleShape).size(40.dp)
                                    ) { Icon(Icons.Default.Add, null, tint = StatusColors.Green, modifier = Modifier.size(20.dp)) }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    IconButton(
                                        onClick = { editingItem = item },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(40.dp)
                                    ) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f), CircleShape).size(40.dp)
                                    ) { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) ItemDialog(categories = items.map { it.category }.distinct(), onDismiss = { showAddDialog = false }, onConfirm = { n, c, p, s, t -> viewModel.addItem(n, c, p, s, t); showAddDialog = false })
    if (editingItem != null) ItemDialog(item = editingItem, categories = items.map { it.category }.distinct(), onDismiss = { editingItem = null }, onConfirm = { n, c, p, s, t -> viewModel.updateItem(editingItem!!.copy(name = n, category = c, price = p, currentStock = s, minThreshold = t)); editingItem = null })
    if (restockingItem != null) RestockDialog(item = restockingItem!!, onDismiss = { restockingItem = null }, onConfirm = { q -> viewModel.restockItem(restockingItem!!, q); restockingItem = null })
    if (itemToDelete != null) AlertDialog(onDismissRequest = { itemToDelete = null }, title = { Text("Delete Item", fontWeight = FontWeight.Black) }, text = { Text("Are you sure you want to delete ${itemToDelete?.name}?") }, confirmButton = { Button(onClick = { itemToDelete?.let { viewModel.deleteItem(it) }; itemToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(12.dp)) { Text("Delete", fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } }, shape = RoundedCornerShape(28.dp))
}

@Composable
fun RestockDialog(item: FoodItem, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var qty by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock ${item.name}", fontWeight = FontWeight.Black) },
        text = { 
            OutlinedTextField(
                value = qty, 
                onValueChange = { qty = it }, 
                label = { Text("Quantity to Add") }, 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp)
            ) 
        },
        confirmButton = { Button(onClick = { onConfirm(qty.toIntOrNull() ?: 0) }, shape = RoundedCornerShape(12.dp)) { Text("Restock Now", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDialog(item: FoodItem? = null, categories: List<String>, onDismiss: () -> Unit, onConfirm: (String, String, Double, Int, Int) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Meals") }
    var price by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(item?.currentStock?.toString() ?: "") }
    var threshold by remember { mutableStateOf(item?.minThreshold?.toString() ?: "5") }
    var expanded by remember { mutableStateOf(false) }
    val allCats = (listOf("Meals", "Drinks", "Snacks", "Desserts") + categories).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Menu Item" else "Update Item", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp) )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = category, onValueChange = {}, label = { Text("Category") }, modifier = Modifier.menuAnchor().fillMaxWidth(), readOnly = true, shape = RoundedCornerShape(16.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        allCats.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; expanded = false }) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₱)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
                }
                OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text("Low Stock Alert Threshold") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
            }
        },
        confirmButton = { 
            Button(
                onClick = { onConfirm(name, category, price.toDoubleOrNull() ?: 0.0, stock.toIntOrNull() ?: 0, threshold.toIntOrNull() ?: 5) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text(if (item == null) "Create Item" else "Save Changes", fontWeight = FontWeight.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } },
        shape = RoundedCornerShape(28.dp)
    )
}
