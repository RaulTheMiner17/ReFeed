package com.ran.refeed.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ran.refeed.viewmodels.AddFoodDonationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDonationScreen(
    navController: NavController,
    viewModel: AddFoodDonationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val isDetecting by viewModel.isDetecting.collectAsState()
    val detectedFoodName by viewModel.detectedFoodName.collectAsState()
    val alternatives by viewModel.alternativeFoodSuggestions.collectAsState() // Collect alternatives
    val confidence by viewModel.detectionConfidence.collectAsState() // Collect confidence
    val currentLocation by viewModel.currentLocation.collectAsState() // Collect location

    // State for UI elements
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") } // Text quantity (e.g., "2 boxes")
    var quantityNumber by remember { mutableStateOf("1") } // Numerical quantity
    var price by remember { mutableStateOf("0.00") }
    // Default expiry: 7 days from today
    val defaultExpiryMillis = remember { System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7) }
    var expiryDate by remember { mutableLongStateOf(defaultExpiryMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Initialize ViewModel dependencies (Appwrite client, Detection Service)
    LaunchedEffect(key1 = Unit) {
        Log.d("AddFoodScreen", "Initializing ViewModel dependencies...")
        viewModel.initialize(context.applicationContext) // Use application context
    }

    // Update name field when AI detection completes (if name is empty)
    LaunchedEffect(detectedFoodName) {
        if (detectedFoodName.isNotEmpty() && name.isBlank()) {
            name = detectedFoodName
        }
    }

    // Date formatter (remembered)
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let { // Only call detection if URI is not null
            viewModel.detectFoodFromImage(it) // Pass only URI
        }
    }

    // Handle success: Navigate back and reset ViewModel state
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Log.d("AddFoodScreen", "Success detected, navigating back and resetting state.")
            navController.popBackStack()
            viewModel.resetSuccessState() // Reset the flag in ViewModel (Ensure this exists in VM)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Food Donation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp) // Padding for content inside the column
                    .verticalScroll(rememberScrollState()), // Make content scrollable
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Image Selection Box ---
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp)) // Slightly rounder corners
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Subtler border
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") }, // Launch picker on click
                    contentAlignment = Alignment.Center
                ) {
                    // Display Selected Image or Placeholder
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected food image",
                            contentScale = ContentScale.Crop, // Crop to fit
                            modifier = Modifier.fillMaxSize()
                        )
                        // Detection Success Indicator (using AnimatedVisibility directly in Box scope)
                        this@Column.AnimatedVisibility(
                            visible = !isDetecting && detectedFoodName.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopEnd) // Position top-right
                                .padding(8.dp) // Padding from edges
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Detection Successful",
                                tint = Color(0xFF4CAF50), // Explicit Green
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        // Placeholder when no image is selected
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt, // Changed Icon
                                contentDescription = "Add Image Placeholder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to Add Photo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // --- Detecting Indicator Overlay ---
                    this@Column.AnimatedVisibility(
                        visible = isDetecting,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.matchParentSize() // Make overlay fill the Box
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp)), // Match rounding
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("AI Detecting Food...", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Please wait", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } // End Image Selection Box

                Spacer(modifier = Modifier.height(20.dp)) // Increased spacing

                // --- Location Info ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Donation Location",
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (currentLocation != null) {
                                Text(
                                    text = "Lat: ${String.format("%.6f", currentLocation?.latitude)}, " +
                                            "Lng: ${String.format("%.6f", currentLocation?.longitude)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = "Location not available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Food Name TextField ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name *") }, // Indicate required
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        // Show AI detection result and confidence
                        if (detectedFoodName.isNotEmpty() && confidence > 0) {
                            val confidenceText = "AI detected: $detectedFoodName (${(confidence * 100).toInt()}%)"
                            val alternativesText = if (alternatives.isNotEmpty()) " / Alternative: ${alternatives.take(2).joinToString()}" else "" // Abbreviated
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "AI Detected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = confidenceText + alternativesText,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2 // Allow wrapping for alternatives
                                )
                            }
                        } else if (isDetecting) {
                            Text("Detecting name...", style = MaterialTheme.typography.bodySmall)
                        } else if (imageUri != null && detectedFoodName.isEmpty() && !isDetecting) {
                            // Indicate if detection finished with no result
                            Text("AI could not detect food name.", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    trailingIcon = {
                        // Button to use the AI suggestion if different
                        if (detectedFoodName.isNotEmpty() && name != detectedFoodName) {
                            TextButton(
                                onClick = { name = detectedFoodName },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Use") // Shortened text
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // --- Description TextField ---
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") }, // Indicate required
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Minimum height
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))
                // --- Quantity Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top // Align labels to top
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Unit (e.g., boxes) *") }, // Indicate required
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = quantityNumber,
                        onValueChange = { input ->
                            // Allow empty or digits only, filter non-digits
                            quantityNumber = input.filter { it.isDigit() }
                        },
                        label = { Text("Number *") }, // Indicate required
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // --- Price TextField ---
                OutlinedTextField(
                    value = price,
                    onValueChange = { input ->
                        // Allow empty input or numbers with an optional decimal (up to 2 places)
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            price = input
                        }
                    },
                    label = { Text("Price (Rs.) *") }, // Indicate required
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // --- Expiry Date TextField ---
                OutlinedTextField(
                    value = dateFormatter.format(Date(expiryDate)), // Format the selected date
                    onValueChange = { }, // Read-only field
                    label = { Text("Expiry Date *") }, // Indicate required
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Expiry Date")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                // --- Submit Button ---
                val canSubmit = remember(name, description, quantity, quantityNumber, price, imageUri, isLoading, isDetecting) {
                    !isLoading && !isDetecting &&
                            name.isNotBlank() &&
                            description.isNotBlank() &&
                            quantity.isNotBlank() &&
                            (quantityNumber.toIntOrNull() ?: 0) > 0 && // Ensure number is positive
                            (price.toDoubleOrNull()) != null && // Ensure price is a valid double (allows 0.0)
                            imageUri != null // Image must be selected
                }
                Button(
                    onClick = {
                        val finalQuantityNumber = quantityNumber.toIntOrNull() ?: 1 // Fallback should ideally not be needed due to validation
                        val finalPrice = price.toDoubleOrNull() ?: 0.0 // Fallback
                        // Call ViewModel function to add donation
                        viewModel.addFoodDonation(
                            context = context, // Context needed for image upload within VM
                            name = name.trim(),
                            description = description.trim(),
                            quantity = quantity.trim(),
                            quantityNumber = finalQuantityNumber,
                            price = finalPrice,
                            expiryDate = expiryDate,
                            imageUri = imageUri!! // Use non-null assertion as imageUri is checked in `canSubmit`
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canSubmit // Enable based on validation state
                ) {
                    // Show progress or text based on isLoading state
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit Donation")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Add some padding at the bottom
            } // End Column
            // --- Loading overlay for submission ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false, onClick = {}), // Prevent clicks behind overlay
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Submitting...", color = Color.White)
                    }
                }
            }
        } // End Outer Box
    } // End Scaffold

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        // Calculate today's start of day in UTC milliseconds
        val today = remember {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate.coerceAtLeast(today), // Ensure initial is not past
            selectableDates = object : SelectableDates {
                // Allow selection only from today onwards
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Only update if the selected date is valid
                            if (selectedDate >= today) {
                                expiryDate = selectedDate
                            }
                        }
                        showDatePicker = false // Close dialog
                    },
                    // Enable confirm only if a date is selected
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState) // Show the DatePicker composable
        }
    }
} // End Composable Function

