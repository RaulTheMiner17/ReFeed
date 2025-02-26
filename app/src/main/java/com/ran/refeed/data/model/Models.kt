// data/models/Models.kt
package com.ran.refeed.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = ""
) {
    constructor() : this("", "", "")
}


data class Shelter(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val contact: String = ""
)

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val donorId: String = "",
    val expiryDate: Long = 0,
    val status: String = "available"
)