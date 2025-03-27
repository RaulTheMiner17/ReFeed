package com.ran.refeed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
// import androidx.compose.foundation.shape.CircleShape // Not used directly in this modified version
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip // Not used directly in this modified version
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.painterResource // Not used directly in this modified version
import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign // Not used directly in this modified version
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
// import com.ran.refeed.R // Not used directly in this modified version
import com.ran.refeed.data.model.FoodItem
import com.ran.refeed.data.model.Shelter
import com.ran.refeed.viewmodels.CartViewModel
import com.ran.refeed.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
// import com.ran.refeed.ui.components.BottomNavigationBar // Import was present but unused in original Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val foodItems by homeViewModel.foodItems.collectAsState()
    val shelters by homeViewModel.shelters.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReFeed") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        // Note: The original code imported BottomNavigationBar but didn't use it here.
        // If you need a bottom bar, you can add it like in the second example:
        // bottomBar = { BottomNavigationBar(navController, cartViewModel) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Section Moved: Nearby Shelters ---
            if (shelters.isNotEmpty()) {
                item {
                    SectionTitle("Nearby Shelters")
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(shelters) { shelter ->
                            // ShelterCard is clickable due to the modifier in its definition
                            // and the onClick lambda passed here.
                            ShelterCard(
                                shelter = shelter,
                                onClick = { navController.navigate("donationCenter/${shelter.id}") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer slightly
                }
            }
            // --- End of Moved Section ---

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        homeViewModel.searchFoodItems(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Adjusted padding
                    placeholder = { Text("Search for food...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                homeViewModel.fetchFoodItems() // Refetch all items when clearing
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Food Items Section
            item {
                SectionTitle("Available Food")
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50)) // Use theme color if available
                    }
                }
            } else if (foodItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp), // Add padding for empty state
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No food items match your search." else "No food items available right now.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray // Use theme color if available
                        )
                    }
                }
            } else {
                items(foodItems) { foodItem ->
                    FoodItemCard(
                        foodItem = foodItem,
                        onClick = { navController.navigate("foodDetail/${foodItem.id}") },
                        onAddToCart = { cartViewModel.addToCart(foodItem) }
                    )
                }
            }

            // Add some space at the bottom for better scrolling with potential bottom bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp) // Adjusted padding
    )
}

@Composable
fun ShelterCard(shelter: Shelter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp) // Consider making width dynamic or smaller if needed
            .clickable(onClick = onClick), // Clickable modifier applied here
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Shelter Image
            if (shelter.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(shelter.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_placeholder) // Add a placeholder drawable
                        .error(R.drawable.ic_placeholder) // Add an error drawable
                        .build(),
                    contentDescription = shelter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                // Placeholder when no image URL is available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.LightGray), // Use theme color if available
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Shelter Placeholder",
                        tint = Color.White, // Use theme color if available
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = shelter.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1 // Ensure title doesn't wrap excessively
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = shelter.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray, // Use theme color if available
                    maxLines = 2 // Limit address lines
                )
            }
        }
    }
}

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val context = LocalContext.current
    val isExpired = foodItem.expiryDate > 0 && foodItem.expiryDate < System.currentTimeMillis()
    val expiryTextColor = if (isExpired) Color.Red else Color.Gray // Use theme color if available

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick), // Clickable modifier applied here
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Food Image
            if (foodItem.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(foodItem.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_placeholder_food) // Add a food placeholder
                        .error(R.drawable.ic_placeholder_food) // Add an error placeholder
                        .build(),
                    contentDescription = foodItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                // Placeholder when no image URL is available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray), // Use theme color if available
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Food Placeholder",
                        tint = Color.White, // Use theme color if available
                        modifier = Modifier.size(40.dp)
                    )
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top // Align to top for consistency
                ) {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(end = 8.dp) // Allow text to wrap if long
                    )

                    Text(
                        text = "Rs.${foodItem.price}", // Format price if needed
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50), // Use theme color if available
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = foodItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray, // Use theme color if available
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp)) // Increased spacing before details

                // Donor information
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Donor",
                        tint = Color.Gray, // Use theme color if available
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Donated by ${foodItem.donorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray // Use theme color if available
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quantity information
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info, // Consider a more specific icon like Inventory2Outlined
                        contentDescription = "Quantity",
                        tint = Color.Gray, // Use theme color if available
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Quantity: ${foodItem.quantity}", // Add units if applicable (e.g., "servings")
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray // Use theme color if available
                    )
                }

                // Expiry date if available
                if (foodItem.expiryDate > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    val expiryDateString = remember(foodItem.expiryDate) { dateFormat.format(Date(foodItem.expiryDate)) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Expiry Date",
                            tint = expiryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Expires on: $expiryDateString",
                            style = MaterialTheme.typography.bodySmall,
                            color = expiryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp)) // Increased spacing before button

                // Add to cart button
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Use theme color
                    enabled = !isExpired // Disable button if item is expired
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null, // Button text describes action
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(if (isExpired) "Expired" else "Add to Cart")
                }
            }
        }
    }
}

// Placeholder drawables (replace with your actual resources)
object R {
    object drawable {
        const val ic_placeholder = android.R.drawable.ic_menu_gallery // Example placeholder
        const val ic_placeholder_food = android.R.drawable.ic_menu_recent_history // Example placeholder
    }
}