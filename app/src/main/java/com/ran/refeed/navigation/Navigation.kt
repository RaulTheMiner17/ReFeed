package com.ran.refeed.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ran.refeed.ui.screens.CartScreen
import com.ran.refeed.ui.screens.CategoriesScreen
import com.ran.refeed.ui.screens.CheckoutScreen
import com.ran.refeed.ui.screens.DonationCenterDetailScreen
import com.ran.refeed.ui.screens.FoodDetailScreen
import com.ran.refeed.ui.screens.HomeScreen
import com.ran.refeed.ui.screens.LoginScreen
import com.ran.refeed.ui.screens.OrderConfirmationScreen
import com.ran.refeed.ui.screens.RegisterScreen
import com.ran.refeed.viewmodels.AuthViewModel
import com.ran.refeed.viewmodels.CartViewModel

@Composable
fun ReFeedNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
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
            HomeScreen(navController = navController)
        }

        composable("categories") {
            CategoriesScreen()
        }

        // Food detail screen with foodId parameter
        composable(
            route = "foodDetail/{foodId}",
            arguments = listOf(
                navArgument("foodId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getInt("foodId") ?: 1
            FoodDetailScreen(
                navController = navController,
                foodId = foodId,
                cartViewModel = cartViewModel
            )
        }

        // Donation center detail screen with centerId parameter
        composable(
            route = "donationCenter/{centerId}",
            arguments = listOf(
                navArgument("centerId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val centerId = backStackEntry.arguments?.getInt("centerId") ?: 1
            DonationCenterDetailScreen(navController, centerId)
        }

        // Cart screen
        composable("cart") {
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }

        // Checkout screen
        composable("checkout") {
            CheckoutScreen(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }

        // Order confirmation screen
        composable("orderConfirmation") {
            OrderConfirmationScreen(navController)
        }
    }
}