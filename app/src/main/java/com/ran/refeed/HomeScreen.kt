// ui/screens/HomeScreen.kt
package com.ran.refeed.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ran.refeed.R // Replace with your actual R file
import com.ran.refeed.ui.theme.ReFeedTheme // Replace with your project's theme if you have one


@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = {
            BottomNavigationBar()
        }
    ) { innerPadding ->
        DonationCentersList(innerPadding)
    }
}

@Composable
fun BottomNavigationBar() {
    BottomAppBar(
        containerColor = Color(0xFF4CAF50), // Set the background color to green
        contentColor = Color.White, // Set the content color to white (or your desired color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Navigate to Home */ }) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = { /* TODO: Navigate to Categories */ }) {
                Icon(Icons.Outlined.Category, contentDescription = "Categories")
            }
            IconButton(onClick = { /* TODO: Navigate to Search */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* TODO: Navigate to Profile */ }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
            }
        }
    }
}


@Composable
fun DonationCentersList(paddingValues: PaddingValues) {
    // Dummy data for demonstration.  Replace with your actual data.
    val donationCenters = listOf(
        DonationCenter("Center 1", R.drawable.center1), // Use a placeholder image, replace later.
        DonationCenter("Center 2", R.drawable.center2),
        DonationCenter("Center 3", R.drawable.center3),

        )

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Column {
            Text(
                text = "Donation Centers",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(donationCenters) { center ->
                    DonationCenterItem(center)
                }
            }
        }

    }
}

@Composable
fun DonationCenterItem(center: DonationCenter) {
    Image(
        painter = painterResource(id = center.imageResId),
        contentDescription = "Donation Center Image",
        modifier = Modifier
            .size(width = 200.dp, height = 150.dp) // Adjust size as needed
            .clip(RoundedCornerShape(8.dp)), // Rounded corners for the image
        contentScale = ContentScale.Crop // Crop the image to fill the bounds
    )
}

// Data class for the Donation Center (replace with your actual data structure)
data class DonationCenter(val name: String, val imageResId: Int)



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ReFeedTheme { // Use your project's theme
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    ReFeedTheme {
        BottomNavigationBar()
    }
}

@Preview
@Composable
fun DonationCentersListPreview(){
    ReFeedTheme {
        DonationCentersList(paddingValues = PaddingValues(0.dp))
    }
}

@Preview
@Composable
fun DonationCenterItemPreview(){
    ReFeedTheme {
        DonationCenterItem(center = DonationCenter("center", R.drawable.center1))
    }
}