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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.ran.refeed.ui.theme.ReFeedTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


data class Restaurant(val name: String, val latLng: GeoPoint, val hasExcessFood: Boolean) // Use GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) } // Use GeoPoint
    var showRationale by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize osmdroid configuration (do this *once*, ideally in your Application class)
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val restaurants = remember {
        listOf(
            Restaurant("Restaurant A", GeoPoint(37.7749, -122.4194), true), // San Francisco
            Restaurant("Restaurant B", GeoPoint(37.7850, -122.4060), false),
            Restaurant("Restaurant C", GeoPoint(37.7650, -122.4290), true),
            Restaurant("Restaurant D", GeoPoint(40.7128, -74.0060), true),  //New York
            Restaurant("Restaurant E", GeoPoint(40.7228, -74.0160), false),
            Restaurant("Restaurant F", GeoPoint(40.7028, -73.9960), true),
        )
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
            if (locationPermissionGranted) {
                // OSM Map
                OSMMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentLocation = currentLocation,
                    restaurants = restaurants,
                    onMarkerClick = { restaurant ->
                        // Navigate to donation center detail
                        navController.navigate("donationCenter/${restaurant.name}")
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
                        Button(onClick = {permanentlyDenied = false}){
                            Text("Cancel")
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Location permission required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}


@Composable
fun OSMMapView(
    modifier: Modifier = Modifier,
    currentLocation: GeoPoint?,
    restaurants: List<Restaurant>,
    onMarkerClick: (Restaurant) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }  // Use remember for the MapView

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mapView ->
            mapView.setTileSource(TileSourceFactory.MAPNIK) // Or other tile sources
            mapView.controller.setZoom(15.0)  // Initial zoom level
            mapView.setMultiTouchControls(true)

            // Clear existing overlays to prevent duplicates
            mapView.overlays.clear()

            currentLocation?.let {
                mapView.controller.animateTo(it) // Center on current location
                val locationMarker = Marker(mapView)
                locationMarker.position = it
                locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                locationMarker.title = "Your Location"
                mapView.overlays.add(locationMarker)
            }

            // Add restaurant markers
            for (restaurant in restaurants) {
                if (restaurant.hasExcessFood) {
                    val marker = Marker(mapView)
                    marker.position = restaurant.latLng
                    marker.title = restaurant.name
                    marker.snippet = "Has excess food" // Optional snippet
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Center the marker

                    // Set marker click listener
                    marker.setOnMarkerClickListener { marker, mapView ->
                        onMarkerClick(restaurant)
                        true // Return true to consume the event
                    }

                    mapView.overlays.add(marker)
                }
            }

            mapView.onResume()
        }
    )
}

@SuppressLint("MissingPermission")
private fun getLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    callback: (GeoPoint) -> Unit // Changed to GeoPoint
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                callback(GeoPoint(location.latitude, location.longitude)) // Convert to GeoPoint
            }
        }
        .addOnFailureListener {
            // Handle failure
        }
}

@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    ReFeedTheme {
        CategoriesScreen(rememberNavController())
    }
}