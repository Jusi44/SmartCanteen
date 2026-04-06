package com.example.smartcanteen.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartcanteen.data.FoodItem

@Composable
fun RestockDialog(item: FoodItem, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var qty by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("Restock ${item.name}", fontWeight = FontWeight.Bold) }, 
        text = { 
            OutlinedTextField(
                value = qty, 
                onValueChange = { qty = it }, 
                label = { Text("Add Quantity") }, 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp)
            ) 
        }, 
        confirmButton = { 
            Button(onClick = { onConfirm(qty.toIntOrNull() ?: 0) }, shape = RoundedCornerShape(12.dp)) { 
                Text("Add to Stock") 
            } 
        }, 
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            } 
        }
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
        title = { Text(if (item == null) "New Menu Item" else "Edit Menu Item", fontWeight = FontWeight.ExtraBold) }, 
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp) )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category, 
                        onValueChange = {}, 
                        label = { Text("Category") }, 
                        modifier = Modifier.menuAnchor().fillMaxWidth(), 
                        readOnly = true, 
                        shape = RoundedCornerShape(16.dp), 
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        allCats.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; expanded = false }) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
                }
                OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text("Low Stock Alert Level") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(16.dp))
            }
        }, 
        confirmButton = { 
            Button(onClick = { onConfirm(name, category, price.toDoubleOrNull() ?: 0.0, stock.toIntOrNull() ?: 0, threshold.toIntOrNull() ?: 5) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { 
                Text("Save Item", fontWeight = FontWeight.Bold) 
            } 
        }, 
        dismissButton = { 
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text("Cancel") 
            } 
        }
    )
}
