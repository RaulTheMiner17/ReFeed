// navigation/Navigation.kt
package com.ran.refeed.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ran.refeed.ui.screens.HomeScreen  // Import HomeScreen
import com.ran.refeed.ui.screens.LoginScreen
import com.ran.refeed.ui.screens.RegisterScreen
import com.ran.refeed.ui.screens.CategoriesScreen // Import CategoriesScreen
import com.ran.refeed.viewmodels.AuthViewModel

@Composable
fun ReFeedNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"  // Correct start destination
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") { // Navigate to "home" after login
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
                    navController.navigate("home") { // Navigate to "home" after registration
                        popUpTo("register") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable("home") { // Use "home" as the route
            HomeScreen(navController = navController) // Pass the navController
        }

        composable("categories") { // Add the categories route
            CategoriesScreen()
        }
    }
}