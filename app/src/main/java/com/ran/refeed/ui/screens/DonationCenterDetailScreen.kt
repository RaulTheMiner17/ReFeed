package com.ran.refeed.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ran.refeed.R
import com.ran.refeed.ui.theme.ReFeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationCenterDetailScreen(navController: NavController, centerId: Int) {
    // In a real app, you would fetch the center details based on the centerId
    // For this example, we'll create a mock center
    val center = when (centerId) {
        1 -> DonationCenter(1, "Center 1", R.drawable.center1, "123 Main St",
            "A center dedicated to helping those in need. We collect food donations from individuals and businesses to distribute to people experiencing food insecurity in our community. Our mission is to ensure no one goes hungry.", "9 AM - 5 PM")
        2 -> DonationCenter(2, "Center 2", R.drawable.center2, "456 Oak Ave",
            "Community-focused donation center for all. We believe in the power of community support and work to connect those with resources to those in need. We accept all types of food donations and ensure they reach those who need them most.", "8 AM - 6 PM")
        3 -> DonationCenter(3, "Center 3", R.drawable.center3, "789 Pine Rd",
            "Providing support to local communities. Our center focuses on local needs and works closely with neighborhood organizations to identify and address food insecurity. We welcome volunteers and donations of all kinds.", "10 AM - 4 PM")
        else -> DonationCenter(1, "Center 1", R.drawable.center1, "123 Main St",
            "A center dedicated to helping those in need", "9 AM - 5 PM")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation Center") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(            modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = center.imageResId),
                    contentDescription = "Donation Center Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = center.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Address section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Address",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Text(
                        text = center.address,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Hours section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Hours",
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Text(
                        text = center.hours,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = center.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How to Donate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You can donate food items by visiting our center during operating hours. We accept non-perishable food items, fresh produce, and prepared meals that meet our safety guidelines.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Items we need most:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "• Canned goods\n• Rice and pasta\n• Cooking oils\n• Fresh fruits and vegetables\n• Bread and baked goods",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DonationCenterDetailScreenPreview() {
    ReFeedTheme {
        DonationCenterDetailScreen(rememberNavController(), 1)
    }
}
