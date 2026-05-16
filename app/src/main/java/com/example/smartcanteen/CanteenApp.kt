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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartcanteen.ui.screens.*
import com.example.smartcanteen.ui.theme.GradientEnd
import com.example.smartcanteen.ui.theme.GradientStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCanteenApp(viewModel: CanteenViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    val mainGradient = Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    LaunchedEffect(currentUser) {
        currentUser?.let {
            snackbarHostState.showSnackbar(
                message = "Welcome back, ${it.username}!",
                duration = SnackbarDuration.Short
            )
        }
    }

    AnimatedContent(
        targetState = currentUser,
        transitionSpec = {
            (fadeIn(animationSpec = tween(600)) + slideInVertically { it / 2 })
                .togetherWith(fadeOut(animationSpec = tween(400)))
        },
        label = "login_transition"
    ) { user ->
        if (user == null) {
            LoginScreen { username, password, onResult ->
                viewModel.login(username, password, onResult)
            }
        } else {
            val navItems = remember {
                listOf(
                    Triple("dashboard", "Dashboard", Icons.Default.GridView),
                    Triple("inventory", "Inventory", Icons.Default.RestaurantMenu),
                    Triple("sales", "Orders", Icons.Default.PointOfSale),
                    Triple("reports", "Analytics", Icons.Default.Analytics)
                )
            }
            
            val finalNavItems = remember(user) {
                if (user.role == "ADMIN") {
                    navItems + Triple("users", "Personnel", Icons.Default.AdminPanelSettings)
                } else {
                    navItems
                }
            }

            val pagerState = rememberPagerState(pageCount = { finalNavItems.size })

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(mainGradient)
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentRoute == "stock_history") {
                                    IconButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                } else {
                                    Surface(
                                        modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape),
                                        shape = CircleShape,
                                        color = Color.White
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                user.username.take(1).uppercase(),
                                                fontWeight = FontWeight.Black,
                                                color = GradientEnd,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                }
                                Column {
                                    Text(
                                        "SmartCanteen",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.5).sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "Session: ${user.username}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .clip(CircleShape)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(mainGradient)
                    ) {
                        NavigationBar(
                            tonalElevation = 0.dp,
                            containerColor = Color.Transparent,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            finalNavItems.forEachIndexed { index, item ->
                                val isSelected = (currentRoute == "main" || currentRoute == null) && pagerState.currentPage == index
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = item.third,
                                            contentDescription = item.second,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(item.second, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                                    selected = isSelected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = Color.White.copy(alpha = 0.25f),
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
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
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color(0xFFF9F9FB))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                        userScrollEnabled = currentRoute == "main" || currentRoute == null
                    ) { page ->
                        when (finalNavItems[page].first) {
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
                        enterTransition = { fadeIn(tween(400)) + slideInHorizontally { it / 2 } },
                        exitTransition = { fadeOut(tween(400)) + slideOutHorizontally { -it / 2 } },
                        popEnterTransition = { fadeIn(tween(400)) + slideInHorizontally { -it / 2 } },
                        popExitTransition = { fadeOut(tween(400)) + slideOutHorizontally { it / 2 } }
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
