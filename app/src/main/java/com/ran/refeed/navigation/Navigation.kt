package com.ran.refeed.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

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
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
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
                HomeScreen(navController = navController, cartViewModel = cartViewModel)
            }

            composable("profile") {
                ProfileScreen(navController = navController)
            }

            composable("cart") {
                CartScreen(navController = navController, cartViewModel = cartViewModel)
            }

            composable("donations") {
                CategoriesScreen(navController = navController)
            }

            composable("checkout") {
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
                OrderConfirmationScreen(navController = navController)
            }
        }
    }
}