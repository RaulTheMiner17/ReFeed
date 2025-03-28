package com.ran.refeed.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.ran.refeed.data.state.AuthState
import com.ran.refeed.viewmodels.AuthViewModel

@Composable
fun AuthStateHandler(
    navController: NavController,
    authViewModel: AuthViewModel,
    startDestination: String
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(key1 = authState, key2 = currentUser) {
        when {
            // If user is logged in, navigate to home
            currentUser != null && authState is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            // If user is not logged in and we're not in initial state, stay on login screen
            currentUser == null && authState !is AuthState.Initial -> {
                // Do nothing, stay on login screen
            }
            // Initial app launch, check if user is already logged in
            authState is AuthState.Initial -> {
                if (authViewModel.isUserLoggedIn()) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.navigate(startDestination)
                }
            }
        }
    }
}