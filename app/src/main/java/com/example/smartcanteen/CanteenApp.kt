package com.example.smartcanteen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartcanteen.data.FoodItem
import com.example.smartcanteen.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Centralized Status Colors - Refined for requested logic and maximum visibility
object StatusColors {
    val Red = Color(0xFFD32F2F)    // Vibrant Red
    val Orange = Color(0xFFEF6C00) // Deep Orange (Ensures it doesn't look yellow)
    val Green = Color(0xFF2E7D32)  // Vibrant Green
    
    val RedText = Color(0xFFB71C1C) 
    val OrangeText = Color(0xFFE65100)
    val GreenText = Color(0xFF1B5E20)
    
    val RedBg = Color(0xFFFFEBEE)
    val OrangeBg = Color(0xFFFFF3E0)
    val GreenBg = Color(0xFFE8F5E9)

    @Composable
    fun getStockColor(stock: Int, threshold: Int): Color {
        return when {
            stock <= threshold -> Red    // Red if it is "Low" (at or below threshold)
            stock <= threshold + 5 -> Orange // Orange if it is "Close to Low" (within 5 units)
            else -> Green // Green if it is "Not Low"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCanteenApp(viewModel: CanteenViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            snackbarHostState.showSnackbar(
                message = "Login Successful! Welcome, ${it.username}.",
                duration = SnackbarDuration.Short
            )
        }
    }

    AnimatedContent(
        targetState = currentUser,
        transitionSpec = { 
            (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.92f))
                .togetherWith(fadeOut(animationSpec = tween(500)))
        },
        label = "login_transition"
    ) { user ->
        if (user == null) {
            LoginScreen { username, password, onResult ->
                viewModel.login(username, password, onResult)
            }
        } else {
            val navItems = mutableListOf(
                Triple("dashboard", "Home", Icons.Default.GridView),
                Triple("inventory", "Items", Icons.Default.RestaurantMenu),
                Triple("sales", "Sale", Icons.Default.PointOfSale),
                Triple("reports", "Report", Icons.Default.Analytics)
            )
            
            if (user.role == "ADMIN") {
                navItems.add(Triple("users", "Users", Icons.Default.AdminPanelSettings))
            }

            val pagerState = rememberPagerState(pageCount = { navItems.size })

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).statusBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentRoute == "stock_history") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                user.username.take(1).uppercase(),
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Column {
                                    Text(
                                        "SmartCanteen",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            user.username.lowercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Theme Toggle
                                IconButton(
                                    onClick = {
                                        val nextMode = when (themeMode) {
                                            ThemeMode.AUTO -> ThemeMode.LIGHT
                                            ThemeMode.LIGHT -> ThemeMode.DARK
                                            ThemeMode.DARK -> ThemeMode.AUTO
                                        }
                                        viewModel.setThemeMode(nextMode)
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = when (themeMode) {
                                            ThemeMode.AUTO -> Icons.Default.BrightnessAuto
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = "Toggle Theme",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                IconButton(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(
                        tonalElevation = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.shadow(16.dp)
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = (currentRoute == "main" || currentRoute == null) && pagerState.currentPage == index
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = item.third, 
                                        contentDescription = item.second,
                                        modifier = Modifier.size(26.dp)
                                    ) 
                                },
                                label = { Text(item.second, fontWeight = FontWeight.Bold) },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                onClick = {
                                    if (currentRoute != "main") {
                                        navController.popBackStack("main", false)
                                    }
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                        userScrollEnabled = currentRoute == "main" || currentRoute == null
                    ) { page ->
                        when (navItems[page].first) {
                            "dashboard" -> DashboardScreen(viewModel)
                            "inventory" -> InventoryScreen(viewModel)
                            "sales" -> SalesScreen(viewModel)
                            "reports" -> ReportsScreen(viewModel, navController)
                            "users" -> UsersScreen(viewModel)
                        }
                    }
                    
                    NavHost(
                        navController = navController, 
                        startDestination = "main",
                        enterTransition = { slideInHorizontally { it } + fadeIn() },
                        exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                        popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                    ) {
                        composable("main") { }
                        composable("stock_history") { 
                            Surface(modifier = Modifier.fillMaxSize()) {
                                StockHistoryScreen(viewModel) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String, (Boolean) -> Unit) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition(label = "login_bg")
        val scale by transition.animateFloat(
            initialValue = 1f, targetValue = 1.02f,
            animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "scale"
        )

        Card(
            modifier = Modifier.fillMaxWidth(0.88f).padding(16.dp).scale(scale),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(45.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("SmartCanteen", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("Management System", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(40.dp))
                
                OutlinedTextField(
                    value = username, onValueChange = { username = it; loginError = null },
                    label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = loginError != null,
                    singleLine = true, enabled = !isLoggingIn
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it; loginError = null },
                    label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), isError = loginError != null,
                    singleLine = true, enabled = !isLoggingIn
                )
                
                if (loginError != null) {
                    Text(text = loginError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        isLoggingIn = true
                        onLogin(username, password) { success ->
                            if (!success) {
                                isLoggingIn = false
                                loginError = "Invalid credentials. Please try again."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoggingIn
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIGN IN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: CanteenViewModel) {
    val items by viewModel.allItems.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val lowStockItems = items.filter { it.currentStock <= it.minThreshold }
    
    val topItems = sales.groupBy { it.itemName }
        .mapValues { entry -> entry.value.sumOf { it.quantitySold } }
        .toList().sortedByDescending { it.second }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Dashboard Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), "Total Items", items.size.toString(), Icons.Outlined.Inventory2, MaterialTheme.colorScheme.primaryContainer)
                StatCard(Modifier.weight(1f), "Alerts", lowStockItems.size.toString(), Icons.Outlined.Notifications, if (lowStockItems.isNotEmpty()) StatusColors.RedBg else MaterialTheme.colorScheme.secondaryContainer, if (lowStockItems.isNotEmpty()) StatusColors.RedText else MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        item {
            Text("Sales Trend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            AnimatedSalesChart(sales)
        }

        item {
            Text("Top Performers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        if (topItems.isEmpty()) {
            item { EmptyState(Icons.Default.History, "No sales recorded yet") }
        } else {
            items(topItems.size) { index ->
                val (name, qty) = topItems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("#${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("$qty sold", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }

        if (lowStockItems.isNotEmpty()) {
            item {
                Text("Low Stock Alerts", color = StatusColors.RedText, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            }
            items(lowStockItems) { item ->
                val alertMessage = if (item.currentStock == 0) "OUT OF STOCK" else "Only ${item.currentStock} left"
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    colors = CardDefaults.cardColors(containerColor = StatusColors.RedBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (item.currentStock == 0) Icons.Default.Block else Icons.Default.ErrorOutline, null, tint = StatusColors.RedText)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.name, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.weight(1f), fontSize = 16.sp)
                        Text(alertMessage, fontSize = 14.sp, color = StatusColors.RedText, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun AnimatedSalesChart(sales: List<com.example.smartcanteen.data.Sale>) {
    val last7Days = remember(sales) {
        val cal = Calendar.getInstance()
        val days = mutableListOf<Double>()
        for (i in 0..6) {
            val dayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000
            days.add(sales.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.totalPrice })
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        days.reversed()
    }
    
    val maxVal = (last7Days.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        last7Days.forEach { valPct ->
            val heightPercent by animateFloatAsState(
                targetValue = (valPct / maxVal).toFloat().coerceIn(0.05f, 1f),
                animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "chart"
            )
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(heightPercent)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, textColor: Color = Color.Unspecified) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = color), 
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.5f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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

@Composable
fun RestockDialog(item: FoodItem, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var qty by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Restock ${item.name}", fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Add Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }, confirmButton = { Button(onClick = { onConfirm(qty.toIntOrNull() ?: 0) }, shape = RoundedCornerShape(12.dp)) { Text("Add to Stock") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
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

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (item == null) "New Menu Item" else "Edit Menu Item", fontWeight = FontWeight.ExtraBold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp) )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = category, onValueChange = {}, label = { Text("Category") }, modifier = Modifier.menuAnchor().fillMaxWidth(), readOnly = true, shape = RoundedCornerShape(16.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
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
    }, confirmButton = { Button(onClick = { onConfirm(name, category, price.toDoubleOrNull() ?: 0.0, stock.toIntOrNull() ?: 0, threshold.toIntOrNull() ?: 5) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Save Item", fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } })
}

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

@Composable
fun UsersScreen(viewModel: CanteenViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Authorized Personnel", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = { showAddUserDialog = true }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (users.isEmpty()) {
            EmptyState(Icons.Default.PeopleOutline, "No extra staff accounts")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(users, key = { it.id }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(), 
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = if (user.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person, null, tint = if (user.role == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            },
                            headlineContent = { Text(user.username, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(user.role, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingContent = {
                                if (user.username != "admin") {
                                    IconButton(onClick = { viewModel.deleteUser(user) }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { u, p, r ->
                viewModel.addUser(u, p, r)
                showAddUserDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Personnel", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = role, onValueChange = {}, label = { Text("Access Role") }, modifier = Modifier.menuAnchor().fillMaxWidth(), readOnly = true, shape = RoundedCornerShape(16.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("STAFF", "ADMIN").forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { role = option; expanded = false }) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(username, password, role) }, modifier = Modifier.fillMaxWidth()) { Text("Create Account") } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }
    )
}

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
