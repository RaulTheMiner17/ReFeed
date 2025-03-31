package com.ran.refeed.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.ran.refeed.ui.theme.ReFeedTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class FoodDonation(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val donorName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hasExcessFood: Boolean = true // Always true for food donations
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var showRationale by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // State for food donations
    var foodDonations by remember { mutableStateOf<List<FoodDonation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Initialize osmdroid configuration (do this *once*, ideally in your Application class)
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Fetch food donations from Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("foodItems")
                    .whereEqualTo("status", "available")
                    .get()
                    .await()

                val donations = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val donorName = doc.getString("donorName") ?: "Anonymous"
                    val latitude = doc.getDouble("latitude") ?: 0.0
                    val longitude = doc.getDouble("longitude") ?: 0.0

                    FoodDonation(
                        id = id,
                        name = name,
                        description = description,
                        donorName = donorName,
                        latitude = latitude,
                        longitude = longitude
                    )
                }

                foodDonations = donations
                isLoading = false
            } catch (e: Exception) {
                // Handle error
                isLoading = false
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationPermissionGranted = true
            getLastLocation(fusedLocationClient) { latLng ->
                currentLocation = latLng
            }
        } else {
            if (context is ComponentActivity) {
                permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (!permanentlyDenied) {
                    showRationale = true
                }
            }
        }
    }

    // Use LifecycleEventObserver
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    getLastLocation(fusedLocationClient) { latLng ->
                        currentLocation = latLng
                    }
                    showRationale = false
                    permanentlyDenied = false
                } else {
                    if (context is ComponentActivity) {
                        permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        if (!permanentlyDenied) {
                            showRationale = true
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial check for permission (on screen start)
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            getLastLocation(fusedLocationClient) { latLng ->
                currentLocation = latLng
            }
        } else {
            if (context is ComponentActivity){
                if(ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)){
                    showRationale = true
                }
                else{
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation Centers") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4CAF50)
                )
            } else if (locationPermissionGranted) {
                // OSM Map
                OSMMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentLocation = currentLocation,
                    foodDonations = foodDonations,
                    onMarkerClick = { donation ->
                        // Navigate to donation center detail
                        navController.navigate("donationCenter/${donation.id}")
                    }
                )
            } else if (showRationale) {
                // Rationale
                AlertDialog(
                    onDismissRequest = {
                        showRationale = false
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    title = { Text("Location Permission Needed") },
                    text = { Text("We need your location to show nearby donation centers.") },
                    confirmButton = {
                        Button(onClick = {
                            showRationale = false
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {showRationale = false}){
                            Text("Cancel")
                        }
                    }
                )
            } else if (permanentlyDenied) {
                // Permanently Denied
                AlertDialog(
                    onDismissRequest = { permanentlyDenied = false },
                    title = { Text("Permission Denied") },
                    text = { Text("Please enable location permission in app settings.") },
                    confirmButton = {
                        Button(onClick = {
                            permanentlyDenied = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { permanentlyDenied = false }) {
                            Text("Cancel")
                        }
                    }
                )
            } else {
                // No permission, no rationale, not permanently denied - show basic UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Location Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "We need location permission to show donation centers near you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }

            // FAB for adding new food donation
            FloatingActionButton(
                onClick = { navController.navigate("addFoodDonation") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(
                    "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (GeoPoint) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(GeoPoint(location.latitude, location.longitude))
        } else {
            // Default location if no last location available
            onLocationReceived(GeoPoint(28.6139, 77.2090)) // Default to Delhi, India
        }
    }.addOnFailureListener {
        // Default location on failure
        onLocationReceived(GeoPoint(28.6139, 77.2090)) // Default to Delhi, India
    }
}

@Composable
fun OSMMapView(
    modifier: Modifier = Modifier,
    currentLocation: GeoPoint?,
    foodDonations: List<FoodDonation>,
    onMarkerClick: (FoodDonation) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    // Configure map when it's first created
    DisposableEffect(Unit) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        onDispose {
            mapView.onDetach()
        }
    }

    // Update map when location or donations change
    LaunchedEffect(currentLocation, foodDonations) {
        mapView.overlays.clear()

        // Add current location marker if available
        currentLocation?.let { location ->
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Your Location"
            marker.snippet = "You are here"
            marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
            mapView.overlays.add(marker)

            // Center map on current location
            val mapController = mapView.controller
            mapController.setZoom(15.0)
            mapController.setCenter(location)
        }

        // Add markers for food donations
        foodDonations.forEach { donation ->
            val donationLocation = GeoPoint(donation.latitude, donation.longitude)
            val marker = Marker(mapView)
            marker.position = donationLocation
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = donation.name
            marker.snippet = donation.description
            marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_info)

            // Handle marker click
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                onMarkerClick(donation)
                true // Consume the event
            }

            mapView.overlays.add(marker)
        }

        // If no current location but have donations, center on first donation
        if (currentLocation == null && foodDonations.isNotEmpty()) {
            val firstDonation = foodDonations.first()
            val donationLocation = GeoPoint(firstDonation.latitude, firstDonation.longitude)
            val mapController = mapView.controller
            mapController.setZoom(15.0)
            mapController.setCenter(donationLocation)
        }

        mapView.invalidate() // Refresh the map
    }

    // Render the map view
    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    ReFeedTheme {
        CategoriesScreen(rememberNavController())
    }
}
