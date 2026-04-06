package com.example.smartcanteen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartcanteen.ui.screens.*
import kotlinx.coroutines.launch

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
