package com.ran.refeed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ran.refeed.data.model.CartItem
import com.ran.refeed.data.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // Create a derived StateFlow for the cart item count using stateIn
    val cartItemCount: StateFlow<Int> = _cartItems
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun addToCart(foodItem: FoodItem) {
        val existingItem = _cartItems.value.find { it.foodItem.id == foodItem.id }

        if (existingItem != null) {
            // If item already exists in cart, increase quantity if below available quantity
            if (existingItem.quantity < foodItem.quantityNumber) {
                _cartItems.update { currentItems ->
                    currentItems.map {
                        if (it.foodItem.id == foodItem.id) {
                            it.copy(quantity = it.quantity + 1)
                        } else {
                            it
                        }
                    }
                }
            }
        } else {
            // Add new item to cart
            _cartItems.update { currentItems ->
                currentItems + CartItem(foodItem, 1)
            }
        }
    }

    fun removeFromCart(foodItemId: String) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.foodItem.id != foodItemId }
        }
    }

    fun updateQuantity(foodItemId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(foodItemId)
            return
        }

        _cartItems.update { currentItems ->
            currentItems.map {
                if (it.foodItem.id == foodItemId) {
                    // Ensure quantity doesn't exceed available quantity
                    val newQuantity = minOf(quantity, it.foodItem.quantityNumber)
                    it.copy(quantity = newQuantity)
                } else {
                    it
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cartItems.value.sumOf { it.foodItem.price * it.quantity }
    }

    // Keep this method for backward compatibility
    fun getCartItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun placeOrder(
        address: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to place an order")
            return
        }

        if (_cartItems.value.isEmpty()) {
            onError("Your cart is empty")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val orderItems = _cartItems.value.map { cartItem ->
            mapOf(
                "id" to cartItem.foodItem.id,
                "name" to cartItem.foodItem.name,
                "description" to cartItem.foodItem.description,
                "quantity" to cartItem.foodItem.quantity,
                "quantityNumber" to cartItem.foodItem.quantityNumber,
                "donorId" to cartItem.foodItem.donorId,
                "donorName" to cartItem.foodItem.donorName,
                "expiryDate" to cartItem.foodItem.expiryDate,
                "status" to cartItem.foodItem.status,
                "imageUrl" to cartItem.foodItem.imageUrl,
                "price" to cartItem.foodItem.price,
                "cartQuantity" to cartItem.quantity
            )
        }

        val order = hashMapOf(
            "userId" to currentUser.uid,
            "items" to orderItems,
            "total" to getCartTotal(),
            "date" to currentDate,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Processing",
            "address" to address,
            "phone" to phone
        )

        // Add order to Firestore
        firestore.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                // Update food items status to "pending" and reduce quantity
                _cartItems.value.forEach { cartItem ->
                    firestore.collection("foodItems")
                        .document(cartItem.foodItem.id)
                        .update(
                            mapOf(
                                "status" to "pending",
                                // Update the remaining quantity
                                "quantityNumber" to (cartItem.foodItem.quantityNumber - cartItem.quantity)
                            )
                        )
                }

                // Update user's order count
                firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val currentOrderCount = userDoc.getLong("ordersCount") ?: 0
                        firestore.collection("users")
                            .document(currentUser.uid)
                            .update("ordersCount", currentOrderCount + 1)
                    }

                // Clear cart and notify success
                clearCart()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to place order")
            }
    }
}