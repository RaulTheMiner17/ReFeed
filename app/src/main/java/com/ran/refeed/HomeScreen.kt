package com.ran.refeed.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ran.refeed.R // Replace with your actual R file
import com.ran.refeed.ui.theme.ReFeedTheme // Replace with your project's theme if you have one
import com.ran.refeed.viewmodels.CartViewModel


@Composable
fun HomeScreen(navController: NavController) { // Add NavController
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController) // Pass NavController
        }
    ) { innerPadding ->
        HomeContent(innerPadding, navController)
    }
}

@Composable
fun HomeContent(paddingValues: PaddingValues, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // Donation Centers Section
        Text(
            text = "Donation Centers",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        DonationCentersList(navController)

        Spacer(modifier = Modifier.height(16.dp))

        // Leftover Food Section
        Text(
            text = "Available Leftover Food",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LeftoverFoodList(navController)
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel()
) {
    BottomAppBar(
        containerColor = Color(0xFF4CAF50),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }

            IconButton(onClick = { navController.navigate("categories") }) {
                Icon(Icons.Outlined.LocationOn, contentDescription = "Categories")
            }

            IconButton(onClick = { /* TODO: Navigate to Search */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }

            // Cart icon with badge
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { navController.navigate("cart") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart), // Add a cart icon to your drawable resources
                        contentDescription = "Cart"
                    )
                }

                // Show badge if cart has items
                if (cartViewModel.getCartItemCount() > 0) {
                    Surface(
                        color = Color.Red,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = cartViewModel.getCartItemCount().toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = { /* TODO: Navigate to Profile */ }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
            }
        }
    }
}


@Composable
fun DonationCentersList(navController: NavController) {
    val donationCenters = listOf(
        DonationCenter(1, "Center 1", R.drawable.center1, "123 Main St", "A center dedicated to helping those in need", "9 AM - 5 PM"),
        DonationCenter(2, "Center 2", R.drawable.center2, "456 Oak Ave", "Community-focused donation center for all", "8 AM - 6 PM"),
        DonationCenter(3, "Center 3", R.drawable.center3, "789 Pine Rd", "Providing support to local communities", "10 AM - 4 PM"),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(donationCenters) { center ->
            DonationCenterItem(center, navController)
        }
    }
}

@Composable
fun DonationCenterItem(center: DonationCenter, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            // Navigate to donation center detail page with the center ID
            navController.navigate("donationCenter/${center.id}")
        }
    ) {
        Image(
            painter = painterResource(id = center.imageResId),
            contentDescription = "Donation Center Image",
            modifier = Modifier
                .size(width = 200.dp, height = 150.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = center.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun LeftoverFoodList(navController: NavController) {
    val leftoverFoods = listOf(
        LeftoverFood(1, "Pizza", "2 slices left", "30 minutes ago", R.drawable.center1, "Delicious homemade pizza with cheese and vegetables", 99.00),
        LeftoverFood(2, "Pasta", "1 serving left", "1 hour ago", R.drawable.center2, "Freshly made pasta with tomato sauce", 99.00),
        LeftoverFood(3, "Salad", "3 servings left", "15 minutes ago", R.drawable.center3, "Healthy garden salad with vinaigrette dressing", 99.00),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(leftoverFoods) { food ->
            LeftoverFoodItem(food, navController)
        }
    }
}

@Composable
fun LeftoverFoodItem(food: LeftoverFood, navController: NavController) {
    Card(
        modifier = Modifier
            .size(width = 180.dp, height = 220.dp)
            .clickable {
                // Navigate to food detail page with the food ID
                navController.navigate("foodDetail/${food.id}")
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = food.imageResId),
                contentDescription = "Food Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = food.quantity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = food.timePosted,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

data class DonationCenter(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val address: String,
    val description: String,
    val hours: String
)

data class LeftoverFood(
    val id: Int,
    val name: String,
    val quantity: String,
    val timePosted: String,
    val imageResId: Int,
    val description: String,
    val price: Double
)


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ReFeedTheme {
        val navController = rememberNavController() // Use rememberNavController in previews
        HomeScreen(navController)
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    ReFeedTheme {
        val navController = rememberNavController()
        BottomNavigationBar(navController)
    }
}

@Preview
@Composable
fun DonationCentersListPreview(){
    ReFeedTheme {
        DonationCentersList(rememberNavController())
    }
}

@Preview
@Composable
fun DonationCenterItemPreview(){
    ReFeedTheme {
        DonationCenterItem(
            center = DonationCenter(
                1,
                "Center 1",
                R.drawable.center1,
                "123 Main St",
                "A center dedicated to helping those in need",
                "9 AM - 5 PM"
            ),
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
fun LeftoverFoodListPreview(){
    ReFeedTheme {
        LeftoverFoodList(rememberNavController())
    }
}

@Preview
@Composable
fun LeftoverFoodItemPreview(){
    ReFeedTheme {
        LeftoverFoodItem(
            food = LeftoverFood(
                1,
                "Pizza",
                "2 slices left",
                "30 minutes ago",
                R.drawable.center1,
                "Delicious homemade pizza with cheese and vegetables",
                99.00
            ),
            navController = rememberNavController()
        )
    }
}