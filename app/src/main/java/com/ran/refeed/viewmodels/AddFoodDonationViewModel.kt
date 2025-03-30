package com.ran.refeed.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ran.refeed.utils.FoodDetectionHelper
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

class AddFoodDonationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Appwrite configuration
    private val appwriteProjectId = "67e68003002cba2842ba"
    private lateinit var appwriteClient: Client
    private lateinit var appwriteStorage: Storage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _detectedFoodName = MutableStateFlow("")
    val detectedFoodName: StateFlow<String> = _detectedFoodName

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting

    // Initialize Appwrite client
    fun initAppwrite(context: Context) {
        appwriteClient = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject(appwriteProjectId)
        appwriteStorage = Storage(appwriteClient)
    }

    fun detectFoodFromImage(context: Context, imageUri: Uri?) {
        if (imageUri == null) return

        viewModelScope.launch {
            try {
                _isDetecting.value = true
                val detectedName = FoodDetectionHelper.detectFoodFromImage(context, imageUri)
                _detectedFoodName.value = detectedName
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isDetecting.value = false
            }
        }
    }

    fun addFoodDonation(
        context: Context,
        name: String,
        description: String,
        quantity: String,
        quantityNumber: Int,
        price: Double,
        expiryDate: Long,
        imageUri: Uri?
    ) {
        // Initialize Appwrite if not already initialized
        if (!::appwriteClient.isInitialized) {
            initAppwrite(context)
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
                val userDocRef = firestore.collection("users").document(currentUser.uid)

                // Get user data for donor name
                val userDoc = userDocRef.get().await()

                // Check if user document exists
                val donorName = if (userDoc.exists()) {
                    userDoc.getString("name") ?: "Anonymous"
                } else {
                    // Create user document if it doesn't exist
                    val newUser = hashMapOf(
                        "name" to (currentUser.displayName ?: "Anonymous"),
                        "email" to (currentUser.email ?: ""),
                        "phone" to "",
                        "address" to "",
                        "donationsCount" to 0,
                        "ordersCount" to 0,
                        "profileImageUrl" to (currentUser.photoUrl?.toString() ?: ""),
                        "createdAt" to System.currentTimeMillis()
                    )
                    userDocRef.set(newUser).await()
                    currentUser.displayName ?: "Anonymous"
                }

                // Upload image if available
                var imageUrl = ""
                if (imageUri != null) {
                    imageUrl = uploadImageToAppwrite(context, imageUri)
                }

                // Create food item document
                val foodItem = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "quantity" to quantity,
                    "quantityNumber" to quantityNumber,
                    "donorId" to currentUser.uid,
                    "donorName" to donorName,
                    "expiryDate" to expiryDate,
                    "status" to "available",
                    "imageUrl" to imageUrl,
                    "price" to price,
                    "timestamp" to System.currentTimeMillis()
                )

                // Add to Firestore
                firestore.collection("foodItems")
                    .add(foodItem)
                    .await()

                // Update user's donation count
                val currentDonations = if (userDoc.exists()) {
                    userDoc.getLong("donationsCount")?.toInt() ?: 0
                } else {
                    0
                }
                userDocRef.update("donationsCount", currentDonations + 1)
                    .await()

                _isSuccess.value = true
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImageToAppwrite(context: Context, imageUri: Uri): String {
        try {
            // Create a temporary file from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Create a bucket ID if you don't have one already
            // For this example, we'll use a predefined bucket ID
            val bucketId = "foodImages" // You should create this bucket in Appwrite console

            // Generate a unique file ID
            val fileId = ID.unique()

            // Create an InputFile from the temp file
            val inputFile = InputFile.fromFile(tempFile)

            // Upload the file to Appwrite Storage
            val result = appwriteStorage.createFile(
                bucketId = bucketId,
                fileId = fileId,
                file = inputFile
            )

            // Clean up the temporary file
            tempFile.delete()

            // Return the URL to the uploaded file
            return "https://cloud.appwrite.io/v1/storage/buckets/$bucketId/files/$fileId/view?project=$appwriteProjectId"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
