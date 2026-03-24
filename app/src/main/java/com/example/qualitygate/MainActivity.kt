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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
    object PreRevision : Screen("pre_revision", "Revisión", Icons.Default.Notifications)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, productViewModel: ProductViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = remember(currentUser) {
        val list = mutableListOf(Screen.Dashboard, Screen.ProductList)
        if (currentUser?.role == UserRole.REVISOR) {
            list.add(Screen.PreRevision)
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
                    onRegisterSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
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
            composable("pre_revision") {
                PreRevisionScreen(
                    productViewModel = productViewModel,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.id}")
                    }
                )
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
                    }
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
