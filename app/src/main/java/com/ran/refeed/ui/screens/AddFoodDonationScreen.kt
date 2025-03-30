package com.ran.refeed.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility // Ensure this is imported
import androidx.compose.animation.fadeIn // Ensure this is imported
import androidx.compose.animation.fadeOut // Ensure this is imported
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ran.refeed.viewmodels.AddFoodDonationViewModel // Ensure this import is correct
import java.text.SimpleDateFormat
import java.util.*

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

    // Initialize Appwrite or other services if needed
    LaunchedEffect(Unit) {
        // viewModel.initAppwrite(context) // Assuming this is correct for your setup
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var quantityNumber by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("0.00") }
    var expiryDate by remember { mutableLongStateOf(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) } // Default 7 days
    var showDatePicker by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Update name based on detection
    LaunchedEffect(detectedFoodName) {
        if (detectedFoodName.isNotEmpty() && name.isEmpty()) {
            name = detectedFoodName
        }
    }

    // Date formatter (remembered)
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            viewModel.detectFoodFromImage(context, uri)
        }
    }

    // Handle success navigation
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
            // Consider resetting isSuccess state in ViewModel here
            // viewModel.resetSuccessState()
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
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Selection Box
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Theme color
                        .border(
                            1.dp, MaterialTheme.colorScheme.outline, // Theme color
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Food Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Detection Success Indicator - Syntactically Correct
                        this@Column.AnimatedVisibility(
                            visible = !isDetecting && detectedFoodName.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Detection Successful",
                                tint = Color(0xFF4CAF50), // Green color
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = CircleShape) // Theme color
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Add Image",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Theme color
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add Photo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Theme color
                            )
                        }
                    }

                    // Detecting Indicator Overlay - Syntactically Correct
                    this@Column.AnimatedVisibility(
                        visible = isDetecting,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "AI Detecting Food...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Please wait",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall // Correct use of style
                                )
                            }
                        }
                    }
                } // End Image Selection Box

                Spacer(modifier = Modifier.height(16.dp))

                // Food Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (detectedFoodName.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "AI Detected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "AI detected: $detectedFoodName",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (detectedFoodName.isNotEmpty() && name != detectedFoodName) {
                            TextButton(
                                onClick = { name = detectedFoodName },
                                contentPadding = PaddingValues(horizontal = 8.dp) // Adjust padding if needed
                            ) {
                                Text("Use Suggestion") // Shortened text
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description TextField
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Use heightIn
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (e.g., boxes)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = quantityNumber,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                quantityNumber = input
                            }
                        },
                        label = { Text("Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price TextField
                OutlinedTextField(
                    value = price,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            price = input
                        }
                    },
                    label = { Text("Price (Rs.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expiry Date TextField
                OutlinedTextField(
                    value = dateFormatter.format(Date(expiryDate)),
                    onValueChange = { },
                    label = { Text("Expiry Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        val finalQuantityNumber = quantityNumber.toIntOrNull() ?: 1
                        val finalPrice = price.toDoubleOrNull() ?: 0.0
                        viewModel.addFoodDonation(
                            context = context,
                            name = name.trim(),
                            description = description.trim(),
                            quantity = quantity.trim(),
                            quantityNumber = finalQuantityNumber,
                            price = finalPrice,
                            expiryDate = expiryDate,
                            imageUri = imageUri
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && !isDetecting && name.isNotBlank() && description.isNotBlank() && quantity.isNotBlank() && quantityNumber.toIntOrNull() != null && quantityNumber.toIntOrNull()!! > 0 && imageUri != null
                ) {
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
            } // End Column

            // Loading overlay for submission
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
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

    // Date Picker Dialog
    if (showDatePicker) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate.coerceAtLeast(today.timeInMillis), // Ensure initial date is not past
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= today.timeInMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Redundant check as selectableDates should prevent this, but safe
                            if (selectedDate >= today.timeInMillis) {
                                expiryDate = selectedDate
                            }
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
} // End Composable Function