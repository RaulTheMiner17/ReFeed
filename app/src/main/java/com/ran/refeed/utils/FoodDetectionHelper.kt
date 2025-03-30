package com.ran.refeed.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class FoodDetectionHelper {

    companion object {
        private val FOOD_CATEGORIES = setOf(
            "fruit", "vegetable", "bread", "rice", "pasta", "meat", "chicken", "fish",
            "salad", "soup", "sandwich", "pizza", "burger", "cake", "dessert", "breakfast",
            "lunch", "dinner", "snack", "apple", "banana", "orange", "tomato", "potato",
            "carrot", "broccoli", "lettuce", "cheese", "egg", "milk", "yogurt"
        )

        suspend fun detectFoodFromImage(context: Context, imageUri: Uri): String {
            return withContext(Dispatchers.IO) {
                try {
                    // Convert URI to bitmap
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

                    // Use ML Kit for image labeling
                    val foodItems = detectLabelsFromBitmap(context, bitmap)

                    // Return the most likely food item or empty string
                    foodItems.firstOrNull() ?: ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
            }
        }

        private suspend fun detectLabelsFromBitmap(context: Context, bitmap: Bitmap): List<String> =
            suspendCancellableCoroutine { continuation ->
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        // Filter for food-related labels and sort by confidence
                        val foodLabels = labels
                            .filter { label ->
                                FOOD_CATEGORIES.any {
                                    label.text.lowercase().contains(it) || it.contains(label.text.lowercase())
                                }
                            }
                            .sortedByDescending { it.confidence }
                            .map { it.text.capitalize() }

                        continuation.resume(foodLabels)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        continuation.resume(emptyList())
                    }
            }
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}