package com.ran.refeed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ran.refeed.data.model.FoodItem
import com.ran.refeed.data.model.Shelter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _shelters = MutableStateFlow<List<Shelter>>(emptyList())
    val shelters: StateFlow<List<Shelter>> = _shelters

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchShelters()
        fetchFoodItems()
    }

    private fun fetchShelters() {
        viewModelScope.launch {
            try {
                val sheltersSnapshot = firestore.collection("shelters")
                    .limit(10)
                    .get()
                    .await()

                val sheltersList = sheltersSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val address = doc.getString("address") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val contact = doc.getString("contact") ?: ""

                    Shelter(
                        id = id,
                        name = name,
                        address = address,
                        description = description,
                        imageUrl = imageUrl,
                        contact = contact
                    )
                }

                _shelters.value = sheltersList
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun fetchFoodItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val foodItemsSnapshot = firestore.collection("foodItems")
                    .whereEqualTo("status", "available")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20)
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

                _foodItems.value = foodItemsList
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchFoodItems(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // If query is empty, fetch all available food items
                if (query.isBlank()) {
                    fetchFoodItems()
                    return@launch
                }

                // Search by name (case insensitive)
                val foodItemsSnapshot = firestore.collection("foodItems")
                    .whereEqualTo("status", "available")
                    .orderBy("name")
                    .startAt(query.lowercase())
                    .endAt(query.lowercase() + "\uf8ff")
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

                _foodItems.value = foodItemsList
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}