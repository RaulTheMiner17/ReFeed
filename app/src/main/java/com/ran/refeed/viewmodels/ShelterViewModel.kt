package com.ran.refeed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ran.refeed.data.model.Shelter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShelterViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _shelter = MutableStateFlow<Shelter?>(null)
    val shelter: StateFlow<Shelter?> = _shelter

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchShelter(shelterId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val shelterDoc = firestore.collection("shelters")
                    .document(shelterId)
                    .get()
                    .await()

                if (shelterDoc.exists()) {
                    val id = shelterDoc.id
                    val name = shelterDoc.getString("name") ?: ""
                    val address = shelterDoc.getString("address") ?: ""
                    val description = shelterDoc.getString("description") ?: ""
                    val imageUrl = shelterDoc.getString("imageUrl") ?: ""
                    val contact = shelterDoc.getString("contact") ?: ""

                    _shelter.value = Shelter(
                        id = id,
                        name = name,
                        address = address,
                        description = description,
                        imageUrl = imageUrl,
                        contact = contact
                    )
                } else {
                    _shelter.value = null
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                _shelter.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}