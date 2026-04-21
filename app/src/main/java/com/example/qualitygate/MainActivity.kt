package com.example.qualitygate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.qualitygate.data.model.UserRole
import com.example.qualitygate.ui.screens.*
import com.example.qualitygate.ui.theme.QualityGateTheme
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QualityGateTheme {
                AppNavigation(authViewModel, productViewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object ProductList : Screen("product_list", "Explorar", Icons.AutoMirrored.Filled.List)
    object MyProjects : Screen("my_projects", "Mis Proyectos", Icons.Default.FolderSpecial)
    object PreRevision : Screen("pre_revision", "Revisión", Icons.Default.FactCheck)
    object Approvals : Screen("approvals", "Validación", Icons.Default.Rule)
    object KPIStats : Screen("kpi_stats", "KPIs", Icons.Default.BarChart)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, productViewModel: ProductViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = remember(currentUser) {
        val list = mutableListOf<Screen>(Screen.Dashboard, Screen.ProductList)
        
        when (currentUser?.role) {
            UserRole.SUPERVISOR -> list.add(Screen.MyProjects)
            UserRole.REVISOR -> list.add(Screen.PreRevision)
            UserRole.APROBADOR -> {
                list.add(Screen.Approvals)
                list.add(Screen.KPIStats)
            }
            null -> {}
        }
        
        list.add(Screen.Profile)
        list
    }

    val showBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                indicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally { it } },
            exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally { -it } }
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register_user") }
                )
            }
            composable("register_user") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {},
                    onBackToLogin = { 
                        navController.navigate("login") {
                            popUpTo("register_user") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register_product") },
                    onNavigateToProductList = { navController.navigate("product_list") },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
            composable("product_list") {
                ProductListScreen(
                    productViewModel = productViewModel,
                    authViewModel = authViewModel,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.id}")
                    },
                    onAddProductClick = { navController.navigate("register_product") }
                )
            }
            composable("my_projects") {
                MyProjectsScreen(
                    productViewModel = productViewModel,
                    authViewModel = authViewModel,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.id}")
                    }
                )
            }
            composable("pre_revision") {
                PreRevisionScreen(
                    productViewModel = productViewModel,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.id}")
                    }
                )
            }
            composable("approvals") {
                ApprovalsScreen(
                    productViewModel = productViewModel,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.id}")
                    }
                )
            }
            composable("kpi_stats") {
                KPIStatsScreen(productViewModel = productViewModel)
            }
            composable(
                route = "product_detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    productViewModel = productViewModel,
                    userRole = currentUser?.role ?: UserRole.SUPERVISOR,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("register_product") {
                RegisterProductScreen(
                    productViewModel = productViewModel,
                    authViewModel = authViewModel,
                    onRegistrationSuccess = { productId ->
                        navController.navigate("product_detail/$productId") {
                            popUpTo("register_product") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
