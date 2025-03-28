package com.ran.refeed.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ran.refeed.ui.components.BottomNavigationBar
import com.ran.refeed.ui.screens.*
import com.ran.refeed.viewmodels.AuthViewModel
import com.ran.refeed.viewmodels.CartViewModel
import kotlinx.coroutines.delay

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // State to track if initial auth check is complete
    var isAuthCheckComplete by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("splash") }

    // Get current route to determine when to show bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // List of routes where we want to show the bottom navigation
    val showBottomNav = listOf(
        "home",
        "cart",
        "profile",
        "donations"
    ).any {
        currentRoute?.startsWith(it) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    navController = navController,
                    cartViewModel = cartViewModel
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash", // Start with splash screen
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash screen that checks authentication
            composable("splash") {
                SplashScreen(
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }

            composable("login") {
                val currentUser by authViewModel.currentUser.collectAsState()

                // If user becomes authenticated while on login screen, navigate to home
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = { navController.navigate("login") },
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable("home") {
                val currentUser by authViewModel.currentUser.collectAsState()

                // Ensure user is authenticated to access home
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }

                HomeScreen(navController = navController, cartViewModel = cartViewModel)
            }

            // Rest of your composables with authentication checks...


            composable("profile") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                }

                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel  // Pass the AuthViewModel
                )
            }


            composable("cart") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("cart") { inclusive = true }
                        }
                    }
                }

                CartScreen(navController = navController, cartViewModel = cartViewModel)
            }

            composable("donations") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("donations") { inclusive = true }
                        }
                    }
                }

                CategoriesScreen(navController = navController)
            }

            composable("checkout") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("checkout") { inclusive = true }
                        }
                    }
                }

                CheckoutScreen(navController = navController, cartViewModel = cartViewModel)
            }

            composable(
                route = "foodDetail/{foodId}",
                arguments = listOf(navArgument("foodId") { type = NavType.StringType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                FoodDetailScreen(
                    navController = navController,
                    foodId = foodId,
                    cartViewModel = cartViewModel
                )
            }

            composable("addFoodDonation") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("addFoodDonation") { inclusive = true }
                        }
                    }
                }

                AddFoodDonationScreen(navController = navController)
            }

            composable(
                route = "donationCenter/{centerId}",
                arguments = listOf(navArgument("centerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val centerId = backStackEntry.arguments?.getString("centerId") ?: ""
                DonationCenterDetailScreen(
                    navController = navController,
                    centerId = centerId
                )
            }

            composable("orderConfirmation") {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("orderConfirmation") { inclusive = true }
                        }
                    }
                }

                OrderConfirmationScreen(navController = navController)
            }
        }
    }
}
