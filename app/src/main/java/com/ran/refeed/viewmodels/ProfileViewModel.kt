package com.ran.refeed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ran.refeed.data.model.FoodItem
import com.ran.refeed.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _myFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val myFoodItems: StateFlow<List<FoodItem>> = _myFoodItems

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchUserProfile()
        fetchMyFoodItems()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userDocRef = firestore.collection("users").document(currentUser.uid)
                    val userDoc = userDocRef.get().await()

                    if (userDoc.exists()) {
                        // Document exists, read the data
                        val name = userDoc.getString("name") ?: ""
                        val email = userDoc.getString("email") ?: ""
                        val phone = userDoc.getString("phone") ?: ""
                        val address = userDoc.getString("address") ?: ""
                        val donationsCount = userDoc.getLong("donationsCount")?.toInt() ?: 0
                        val ordersCount = userDoc.getLong("ordersCount")?.toInt() ?: 0
                        val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

                        _user.value = User(
                            id = currentUser.uid,
                            name = name,
                            email = email,
                            phone = phone,
                            address = address,
                            donationsCount = donationsCount,
                            ordersCount = ordersCount,
                            profileImageUrl = profileImageUrl
                        )
                    } else {
                        // Document doesn't exist, create it
                        val newUser = hashMapOf(
                            "name" to (currentUser.displayName ?: ""),
                            "email" to (currentUser.email ?: ""),
                            "phone" to "",
                            "address" to "",
                            "donationsCount" to 0,
                            "ordersCount" to 0,
                            "profileImageUrl" to (currentUser.photoUrl?.toString() ?: ""),
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Set the document with the user data
                        userDocRef.set(newUser).await()

                        // Update the UI with the new user data
                        _user.value = User(
                            id = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            phone = "",
                            address = "",
                            donationsCount = 0,
                            ordersCount = 0,
                            profileImageUrl = currentUser.photoUrl?.toString() ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyFoodItems() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val foodItemsSnapshot = firestore.collection("foodItems")
                        .whereEqualTo("donorId", currentUser.uid)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val foodItemsList = foodItemsSnapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: ""
                        val description = doc.getString("description") ?: ""
                        val quantity = doc.getString("quantity") ?: ""
                        val quantityNumber = doc.getLong("quantityNumber")?.toInt() ?: 0
                        val donorId = doc.getString("donorId") ?: ""
                        val donorName = doc.getString("donorName") ?: ""
                        val expiryDate = doc.getLong("expiryDate") ?: 0
                        val status = doc.getString("status") ?: "available"
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val price = doc.getDouble("price") ?: 0.0
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                        FoodItem(
                            id = id,
                            name = name,
                            description = description,
                            quantity = quantity,
                            quantityNumber = quantityNumber,
                            donorId = donorId,
                            donorName = donorName,
                            expiryDate = expiryDate,
                            status = status,
                            imageUrl = imageUrl,
                            price = price,
                            timestamp = timestamp
                        )
                    }

                    _myFoodItems.value = foodItemsList
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}