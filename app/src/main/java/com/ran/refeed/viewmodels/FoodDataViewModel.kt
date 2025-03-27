package com.ran.refeed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ran.refeed.data.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FoodDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _foodItem = MutableStateFlow<FoodItem?>(null)
    val foodItem: StateFlow<FoodItem?> = _foodItem

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchFoodItem(foodId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val foodDoc = firestore.collection("foodItems")
                    .document(foodId)
                    .get()
                    .await()

                if (foodDoc.exists()) {
                    val id = foodDoc.id
                    val name = foodDoc.getString("name") ?: ""
                    val description = foodDoc.getString("description") ?: ""
                    val quantity = foodDoc.getString("quantity") ?: ""
                    val quantityNumber = foodDoc.getLong("quantityNumber")?.toInt() ?: 0
                    val donorId = foodDoc.getString("donorId") ?: ""
                    val donorName = foodDoc.getString("donorName") ?: ""
                    val expiryDate = foodDoc.getLong("expiryDate") ?: 0
                    val status = foodDoc.getString("status") ?: "available"
                    val imageUrl = foodDoc.getString("imageUrl") ?: ""
                    val price = foodDoc.getDouble("price") ?: 0.0
                    val timestamp = foodDoc.getLong("timestamp") ?: System.currentTimeMillis()

                    _foodItem.value = FoodItem(
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
                } else {
                    _foodItem.value = null
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                _foodItem.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}