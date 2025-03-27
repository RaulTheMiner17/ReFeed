package com.ran.refeed.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddFoodDonationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    fun addFoodDonation(
        name: String,
        description: String,
        quantity: String,
        quantityNumber: Int,
        price: Double,
        expiryDate: Long,
        imageUri: Uri?
    ) {
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
                val imageUrl = if (imageUri != null) {
                    uploadImage(imageUri)
                } else {
                    ""
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

    private suspend fun uploadImage(imageUri: Uri): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child("food_images/${UUID.randomUUID()}")

        return try {
            val uploadTask = imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
