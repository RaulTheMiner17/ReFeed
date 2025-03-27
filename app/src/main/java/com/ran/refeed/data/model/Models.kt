package com.ran.refeed.data.model

// Enhanced User model with profile information
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val donationsCount: Int = 0,
    val ordersCount: Int = 0,
    val profileImageUrl: String = ""
) {
    constructor() : this("", "", "")
}

// Shelter model (unchanged)
data class Shelter(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val contact: String = ""
)

// Enhanced FoodItem model with more details
data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val quantity: String = "", // Changed to String to allow descriptions like "2 servings"
    val quantityNumber: Int = 0, // Numeric quantity for calculations
    val donorId: String = "",
    val donorName: String = "", // Added donor name for display
    val expiryDate: Long = 0,
    val status: String = "available",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

// Order model for tracking user orders
data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<FoodItem> = emptyList(),
    val total: Double = 0.0,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Processing"
)

// CartItem model for shopping cart
data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int = 1
)

// UserProfile model (can be used alongside User or as a replacement)
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val donationsCount: Int = 0,
    val ordersCount: Int = 0,
    val address: String = "",
    val phone: String = ""
)