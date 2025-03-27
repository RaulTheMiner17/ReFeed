package com.ran.refeed.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ran.refeed.viewmodels.CartViewModel

@Composable
fun BottomNavigationBar(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cartCount = cartViewModel.getCartItemCount()

    // Define green color
    val greenColor = Color(0xFF4CAF50)

    NavigationBar(
        containerColor = greenColor,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = greenColor,
                selectedTextColor = greenColor,
                indicatorColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Donations") },
            label = { Text("Donations") },
            selected = currentRoute == "donations",
            onClick = {
                if (currentRoute != "donations") {
                    navController.navigate("donations")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = greenColor,
                selectedTextColor = greenColor,
                indicatorColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = Color.Red
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = Color.White
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Cart") },
            selected = currentRoute == "cart",
            onClick = {
                if (currentRoute != "cart") {
                    navController.navigate("cart")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = greenColor,
                selectedTextColor = greenColor,
                indicatorColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = greenColor,
                selectedTextColor = greenColor,
                indicatorColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}