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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ran.refeed.ui.theme.ReFeedTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


data class Restaurant(val name: String, val latLng: GeoPoint, val hasExcessFood: Boolean) // Use GeoPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategoriesScreen() {
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
            if (event == Lifecycle.Event.ON_PAUSE) { // onPause for osmdroid
                // Handle map view lifecycle (important for osmdroid)
                // mapView.onPause()  // We'll handle this in the AndroidView update callback

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
        bottomBar = {
            BottomAppBarCategories()
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
                    restaurants = restaurants
                )

            } else if (showRationale) {
                // Rationale
                AlertDialog(
                    onDismissRequest = { showRationale = false
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    title = { Text("Location Permission Needed") },
                    text = { Text("We need your location to show nearby restaurants.") },
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
                Text("Location permission not granted")
            }
        }
    }
}


@Composable
fun OSMMapView(
    modifier: Modifier = Modifier,
    currentLocation: GeoPoint?,
    restaurants: List<Restaurant>
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }  // Use remember for the MapView

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = {
                mapView ->
            mapView.setTileSource(TileSourceFactory.MAPNIK) // Or other tile sources
            mapView.controller.setZoom(15.0)  // Initial zoom level
            mapView.setMultiTouchControls(true)
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
                    mapView.overlays.add(marker)
                }
            }
            mapView.onResume()
        }

    )
}

@Composable
fun BottomAppBarCategories() {
    BottomAppBar(
        containerColor = Color(0xFF4CAF50),
        contentColor = Color.White
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
                Icon(Icons.Outlined.LocationOn, contentDescription = "Categories")
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
        CategoriesScreen()
    }
}
@Preview
@Composable
fun CategoriesBottomNavPreview(){
    ReFeedTheme {
        BottomAppBarCategories()
    }
}