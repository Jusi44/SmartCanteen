package com.example.smartcanteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.CanteenViewModel
import com.example.smartcanteen.data.FoodItem
import com.example.smartcanteen.ui.StatusColors
import com.example.smartcanteen.ui.components.EmptyState
import com.example.smartcanteen.ui.theme.GradientEnd
import com.example.smartcanteen.ui.theme.GradientStart
import kotlinx.coroutines.delay

@Composable
fun SalesScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var showSuccess by remember { mutableStateOf(false) }

    val mainGradient = Brush.verticalGradient(listOf(GradientStart, GradientEnd))

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(
            listOf(Color.White, Color(0xFFFFF3E0).copy(alpha = 0.5f))
        )
    )) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(
                "New Order",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("What would you like to sell?") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GradientEnd) },
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GradientEnd.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (items.isEmpty()) {
                EmptyState(Icons.Default.Storefront, "No items available in inventory")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = selectedItem == item
                        val stockColor = StatusColors.getStockColor(item.currentStock, item.minThreshold)

                        Surface(
                            onClick = { 
                                selectedItem = if (isSelected) null else item
                                quantity = 1
                            },
                            modifier = Modifier.fillMaxWidth().shadow(if (isSelected) 12.dp else 2.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) GradientStart.copy(alpha = 0.05f) else Color.White,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GradientEnd) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("₱${item.price}", fontWeight = FontWeight.Black, color = GradientEnd, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF5F5F5)
                                        ) {
                                            Text(
                                                item.category,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        if (item.currentStock <= 0) "OUT OF STOCK" else "${item.currentStock} left",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = stockColor
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(Icons.Default.CheckCircle, null, tint = GradientEnd, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = selectedItem != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .shadow(32.dp, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("Order Summary", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                                    Text(selectedItem?.name ?: "", fontWeight = FontWeight.Black, fontSize = 20.sp, maxLines = 1)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).padding(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(40.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                                    ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(20.dp)) }
                                    
                                    Text(quantity.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 16.dp))
                                    
                                    IconButton(
                                        onClick = { if (quantity < (selectedItem?.currentStock ?: 0)) quantity++ },
                                        modifier = Modifier.size(40.dp).background(mainGradient, CircleShape).shadow(2.dp, CircleShape)
                                    ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                                }
                            }
                            
                            val total = (selectedItem?.price ?: 0.0) * quantity
                            Button(
                                onClick = {
                                    selectedItem?.let {
                                        viewModel.recordSale(it, quantity)
                                        selectedItem = null
                                        showSuccess = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GradientEnd)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("COMPLETE PAYMENT • ₱$total", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSuccess) {
            LaunchedEffect(Unit) { delay(1500); showSuccess = false }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(220.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("SUCCESS", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}
