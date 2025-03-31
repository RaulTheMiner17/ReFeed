package com.ran.refeed.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ran.refeed.ui.components.BottomNavigationBar
import com.ran.refeed.ui.screens.AddFoodDonationScreen
import com.ran.refeed.ui.screens.CartScreen
import com.ran.refeed.ui.screens.CategoriesScreen
import com.ran.refeed.ui.screens.CheckoutScreen
import com.ran.refeed.ui.screens.DonationCenterDetailScreen
import com.ran.refeed.ui.screens.FoodDetailScreen
import com.ran.refeed.ui.screens.HomeScreen
import com.ran.refeed.ui.screens.LoginScreen
import com.ran.refeed.ui.screens.OrderConfirmationScreen
import com.ran.refeed.ui.screens.ProfileScreen
import com.ran.refeed.ui.screens.RegisterScreen
import com.ran.refeed.ui.screens.SplashScreen
import com.ran.refeed.viewmodels.AddFoodDonationViewModel
import com.ran.refeed.viewmodels.AuthViewModel
import com.ran.refeed.viewmodels.CartViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val addFoodDonationViewModel: AddFoodDonationViewModel = viewModel()

    // State to track if initial auth check is complete
    var isAuthCheckComplete by remember { mutableStateOf(false) }

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

            // Authentication screens
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

            // Main app screens
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
                    authViewModel = authViewModel
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

            // Food detail screen
            composable(
                route = "foodDetail/{foodId}",
                arguments = listOf(navArgument("foodId") { type = NavType.StringType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                val currentUser by authViewModel.currentUser.collectAsState()
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("foodDetail/{foodId}") { inclusive = true }
                        }
                    }
                }
                FoodDetailScreen(
                    navController = navController,
                    foodId = foodId,
                    cartViewModel = cartViewModel
                )
            }

            // Food donation screens
            composable("addFoodDonation") {
                val currentUser by authViewModel.currentUser.collectAsState()
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("addFoodDonation") { inclusive = true }
                        }
                    }
                }
                AddFoodDonationScreen(navController = navController, viewModel = addFoodDonationViewModel)
            }

            composable(
                route = "donationCenter/{donationId}",
                arguments = listOf(navArgument("donationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val donationId = backStackEntry.arguments?.getString("donationId") ?: ""
                val currentUser by authViewModel.currentUser.collectAsState()
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo("donationCenter/{donationId}") { inclusive = true }
                        }
                    }
                }
                DonationCenterDetailScreen(
                    navController = navController,
                    donationId = donationId
                )
            }



            // Order confirmation screen
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

            // My donations screen


            // My requests screen





            }
        }
    }

