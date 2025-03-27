package com.ran.refeed.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ran.refeed.ui.screens.LeftoverFood

class CartViewModel : ViewModel() {
    // Cart items list
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> = _cartItems

    // Total price
    private val _totalPrice = mutableStateOf(0.0)
    val totalPrice = _totalPrice

    // Add item to cart
    fun addToCart(food: LeftoverFood, quantity: Int = 1) {
        // Check if item already exists in cart
        val existingItem = _cartItems.find { it.food.id == food.id }

        if (existingItem != null) {
            // Update quantity if item exists
            val index = _cartItems.indexOf(existingItem)
            _cartItems[index] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            // Add new item if it doesn't exist
            _cartItems.add(CartItem(food, quantity))
        }

        // Update total price
        calculateTotal()
    }

    // Remove item from cart
    fun removeFromCart(foodId: Int) {
        _cartItems.removeIf { it.food.id == foodId }
        calculateTotal()
    }

    // Update item quantity
    fun updateQuantity(foodId: Int, quantity: Int) {
        val item = _cartItems.find { it.food.id == foodId }
        item?.let {
            val index = _cartItems.indexOf(it)
            if (quantity <= 0) {
                _cartItems.removeAt(index)
            } else {
                _cartItems[index] = it.copy(quantity = quantity)
            }
            calculateTotal()
        }
    }

    // Clear cart
    fun clearCart() {
        _cartItems.clear()
        _totalPrice.value = 0.0
    }

    // Calculate total price
    private fun calculateTotal() {
        _totalPrice.value = _cartItems.sumOf { it.food.price * it.quantity }
    }

    // Get cart item count
    fun getCartItemCount(): Int {
        return _cartItems.size
    }
}

// Data class for cart items
data class CartItem(
    val food: LeftoverFood,
    val quantity: Int
)

