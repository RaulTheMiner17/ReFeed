package com.ran.refeed.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.ran.refeed.utils.AdvancedFoodDetectionService // Import the service
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.lang.Exception // Ensure Exception is imported
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import org.osmdroid.util.GeoPoint

class AddFoodDonationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "AddFoodDonationVM" // Added TAG for logging

    // Appwrite configuration
    private val appwriteProjectId = "67e68003002cba2842ba" // Replace with your actual Project ID
    private val appwriteBucketId = "foodImages" // Replace with your actual Bucket ID
    private lateinit var appwriteClient: Client
    private lateinit var appwriteStorage: Storage
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow() // Use asStateFlow for external exposure
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()
    private val _detectedFoodName = MutableStateFlow("")
    val detectedFoodName: StateFlow<String> = _detectedFoodName.asStateFlow()
    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()
    private val _alternativeFoodSuggestions = MutableStateFlow<List<String>>(emptyList())
    val alternativeFoodSuggestions: StateFlow<List<String>> = _alternativeFoodSuggestions.asStateFlow()
    private val _detectionConfidence = MutableStateFlow(0f)
    val detectionConfidence: StateFlow<Float> = _detectionConfidence.asStateFlow()

    // Location state
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    // Make lateinit and ensure initialization
    private lateinit var foodDetectionService: AdvancedFoodDetectionService
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Flag to track initialization
    private var isInitialized = false

    // Initialize Appwrite client and Food Detection Service
    // Call this from your Activity/Fragment onCreate or similar lifecycle point
    fun initialize(context: Context) {
        if (isInitialized) return
        Log.d(TAG, "Initializing ViewModel dependencies...")
        try {
            appwriteClient = Client(context)
                .setEndpoint("https://cloud.appwrite.io/v1") // Your Appwrite endpoint
                .setProject(appwriteProjectId)
            appwriteStorage = Storage(appwriteClient)

            // Initialize the food detection service
            foodDetectionService = AdvancedFoodDetectionService(context.applicationContext) // Use application context

            // Initialize location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // Get current location if permission is granted
            getCurrentLocation()

            isInitialized = true
            Log.d(TAG, "ViewModel dependencies initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewModel dependencies", e)
            // Handle initialization error (e.g., show a message to the user)
        }
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // This will throw SecurityException if permission is not granted
                    val locationResult = Tasks.await(fusedLocationClient.lastLocation)
                    locationResult?.let {
                        _currentLocation.value = GeoPoint(it.latitude, it.longitude)
                        Log.d(TAG, "Current location obtained: ${it.latitude}, ${it.longitude}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current location", e)
                // Location permission might not be granted or location is unavailable
            }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
        Log.d(TAG, "Success state reset.") // Optional logging
    }

    // Detect food from the selected image URI
    fun detectFoodFromImage(imageUri: Uri?) {
        if (!isInitialized) {
            Log.e(TAG, "ViewModel not initialized. Cannot detect food.")
            // Optionally inform the user or try to initialize again
            return
        }
        if (imageUri == null) {
            Log.w(TAG, "detectFoodFromImage called with null URI.")
            return
        }
        viewModelScope.launch {
            try {
                _isDetecting.value = true
                _detectedFoodName.value = "" // Clear previous detection
                _alternativeFoodSuggestions.value = emptyList()
                _detectionConfidence.value = 0f
                Log.i(TAG, "Starting food detection for image: $imageUri")
                // Use the advanced detection service (now guaranteed to be initialized)
                val result = foodDetectionService.detectFood(imageUri)
                // Update UI state flows with results
                _detectedFoodName.value = result.foodName
                _alternativeFoodSuggestions.value = result.alternatives
                _detectionConfidence.value = result.confidence
                Log.i(TAG, "Detection complete. Result: Name='${result.foodName}', " +
                        "Confidence=${String.format("%.2f", result.confidence)}, " +
                        "Alternatives=[${result.alternatives.joinToString()}]")
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting food", e)
                _detectedFoodName.value = "" // Reset on error
                _alternativeFoodSuggestions.value = emptyList()
                _detectionConfidence.value = 0f
                // Optionally set an error state flow for the UI
            } finally {
                _isDetecting.value = false
            }
        }
    }

    // Add the food donation details to Firestore and upload image
    fun addFoodDonation(
        context: Context, // Context might still be needed for image upload
        name: String,
        description: String,
        quantity: String, // e.g., "2 plates", "500g"
        quantityNumber: Int, // Numerical quantity for sorting/filtering if needed
        price: Double,
        expiryDate: Long, // Timestamp
        imageUri: Uri?
    ) {
        if (!isInitialized) {
            Log.e(TAG, "ViewModel not initialized. Cannot add donation.")
            // Optionally inform the user or try to initialize again
            // Consider disabling the submit button until initialized
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false // Reset success state
            var imageUrl = "" // Initialize image URL
            try {
                val currentUser = auth.currentUser ?: throw IllegalStateException("User not authenticated")
                val userDocRef = firestore.collection("users").document(currentUser.uid)
                // Upload image first (if available) - runs in IO context
                if (imageUri != null) {
                    imageUrl = uploadImageToAppwrite(context, imageUri) // Pass context if needed by upload
                    if (imageUrl.isEmpty()) {
                        Log.w(TAG, "Image URI provided but upload failed. Proceeding without image URL.")
                        // Decide if upload failure is critical. Here we proceed without URL.
                    }
                }
                // Fetch user data concurrently or proceed if not strictly needed before write
                // Using await here makes it sequential after image upload
                val userDoc = userDocRef.get().await()
                val donorName = if (userDoc.exists()) {
                    userDoc.getString("name") ?: currentUser.displayName ?: "Anonymous Donor"
                } else {
                    // Handle case where user doc doesn't exist - maybe create it?
                    Log.w(TAG, "User document ${currentUser.uid} not found in Firestore.")
                    currentUser.displayName ?: "Anonymous Donor" // Fallback donor name
                }

                // Get current location
                val location = _currentLocation.value

                // Prepare food item data
                val foodItem = hashMapOf(
                    "name" to name.trim(),
                    "description" to description.trim(),
                    "quantity" to quantity.trim(),
                    "quantityNumber" to quantityNumber,
                    "donorId" to currentUser.uid,
                    "donorName" to donorName,
                    "expiryDate" to expiryDate,
                    "status" to "available", // Default status
                    "imageUrl" to imageUrl, // Use uploaded URL or empty string
                    "price" to price,
                    "timestamp" to System.currentTimeMillis(), // Server timestamp is often better: FieldValue.serverTimestamp()
                    "latitude" to (location?.latitude ?: 0.0), // Add location data
                    "longitude" to (location?.longitude ?: 0.0) // Add location data
                )

                // Add food item to Firestore
                Log.d(TAG, "Adding food item to Firestore...")
                val addedDocRef = firestore.collection("foodItems").add(foodItem).await()
                Log.i(TAG, "Food item added successfully with ID: ${addedDocRef.id}")

                // Update user's donation count (handle potential non-existence)
                try {
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(userDocRef)
                        val currentDonations = snapshot.getLong("donationsCount") ?: 0L
                        transaction.update(userDocRef, "donationsCount", currentDonations + 1)
                        null // Return null to indicate success
                    }.await()
                    Log.d(TAG, "Updated donationsCount for user ${currentUser.uid}")
                } catch (txError: Exception) {
                    Log.e(TAG, "Failed to update user donationsCount transactionally", txError)
                    // Decide how to handle this - maybe log and continue? The food item is already added.
                }
                _isSuccess.value = true // Signal success to UI
            } catch (e: Exception) {
                Log.e(TAG, "Error adding food donation", e)
                _isSuccess.value = false // Ensure success is false on error
                // Optionally set an error state flow for the UI
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Uploads image to Appwrite Storage
    private suspend fun uploadImageToAppwrite(context: Context, imageUri: Uri): String {
        Log.d(TAG, "Starting image upload to Appwrite for URI: $imageUri")
        // Ensure this runs on an appropriate dispatcher (IO)
        return withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                // Create a temporary file from the URI
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    tempFile = File.createTempFile("upload_${UUID.randomUUID()}", ".jpg", context.cacheDir)
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw Exception("Failed to open input stream for URI")
                Log.d(TAG, "Temporary file created: ${tempFile?.absolutePath}")
                // Generate a unique file ID
                val fileId = ID.unique()
                // Create an InputFile from the temp file
                val inputFile = InputFile.fromFile(tempFile!!) // Safe call as exception thrown if null
                // Upload the file
                Log.d(TAG, "Uploading file to Appwrite bucket '$appwriteBucketId' with fileId '$fileId'")
                val result = appwriteStorage.createFile(
                    bucketId = appwriteBucketId,
                    fileId = fileId,
                    file = inputFile
                )
                Log.i(TAG, "Appwrite file upload successful. File ID: ${result.id}")
                // Construct the URL for viewing the file
                val fileUrl = "https://cloud.appwrite.io/v1/storage/buckets/$appwriteBucketId/files/$fileId/view?project=$appwriteProjectId"
                Log.d(TAG, "Constructed file URL: $fileUrl")
                fileUrl // Return the URL
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image to Appwrite", e)
                "" // Return empty string on failure
            } finally {
                // Clean up the temporary file
                tempFile?.let {
                    if (it.exists()) {
                        val deleted = it.delete()
                        Log.d(TAG, "Temporary file deleted: $deleted (${it.absolutePath})")
                    }
                }
            }
        }
    }
}
