package com.ran.refeed.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Advanced food detection service that combines multiple detection methods
 * for more accurate food identification.
 */
class AdvancedFoodDetectionService(private val context: Context) {
    private val TAG = "AdvancedFoodDetection"

    // Food database with common Indian and international foods
    private val foodDatabase = FoodDatabase.getInstance()

    /**
     * Main detection method that combines multiple approaches
     */
    suspend fun detectFood(imageUri: Uri): FoodDetectionResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting advanced food detection")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

                // Run multiple detection methods in parallel
                coroutineScope {
                    val mlKitLabelsDeferred = async { detectWithMlKit(bitmap) }
                    val colorAnalysisDeferred = async { analyzeColorProfile(bitmap) }

                    // Get results from all methods
                    val mlKitLabels = mlKitLabelsDeferred.await()
                    val colorAnalysis = colorAnalysisDeferred.await()

                    // Combine and process results
                    processResults(mlKitLabels, colorAnalysis)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in food detection", e)
                FoodDetectionResult("", 0f, emptyList())
            }
        }
    }

    /**
     * Detect food using ML Kit image labeling
     */
    private suspend fun detectWithMlKit(bitmap: Bitmap): List<Pair<String, Float>> =
        suspendCancellableCoroutine { continuation ->
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.4f)
                .build()

            val labeler = ImageLabeling.getClient(options)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val results = labels.map { Pair(it.text.lowercase(), it.confidence) }
                    Log.d(TAG, "ML Kit detected: ${results.joinToString()}")
                    continuation.resume(results)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit detection failed", e)
                    continuation.resume(emptyList())
                }
        }

    /**
     * Analyze color profile of the image to help identify food types
     * (e.g., green for vegetables, brown for bread/meat)
     */
    private suspend fun analyzeColorProfile(bitmap: Bitmap): ColorProfile {
        return withContext(Dispatchers.Default) {
            // Simple implementation - could be enhanced with more sophisticated analysis
            var redSum = 0
            var greenSum = 0
            var blueSum = 0
            var pixelCount = 0

            // Sample pixels (every 10th pixel to save processing time)
            for (x in 0 until bitmap.width step 10) {
                for (y in 0 until bitmap.height step 10) {
                    val pixel = bitmap.getPixel(x, y)
                    redSum += (pixel shr 16) and 0xFF
                    greenSum += (pixel shr 8) and 0xFF
                    blueSum += pixel and 0xFF
                    pixelCount++
                }
            }

            // Calculate averages
            val avgRed = redSum / pixelCount
            val avgGreen = greenSum / pixelCount
            val avgBlue = blueSum / pixelCount

            // Determine dominant color
            val dominantColor = when {
                avgGreen > avgRed && avgGreen > avgBlue -> "green"
                avgRed > avgGreen && avgRed > avgBlue -> "red"
                avgBlue > avgRed && avgBlue > avgGreen -> "blue"
                avgRed > avgBlue && avgGreen > avgBlue -> "yellow"
                avgRed > avgGreen && avgBlue > avgGreen -> "purple"
                else -> "neutral"
            }

            // Calculate brightness
            val brightness = (0.299 * avgRed + 0.587 * avgGreen + 0.114 * avgBlue) / 255

            ColorProfile(dominantColor, brightness)
        }
    }

    /**
     * Process and combine results from different detection methods
     */
    private fun processResults(
        mlKitLabels: List<Pair<String, Float>>,
        colorProfile: ColorProfile
    ): FoodDetectionResult {
        // Filter out non-food items
        val filteredLabels = mlKitLabels.filter { (label, _) ->
            !NON_FOOD_ITEMS.any { nonFood ->
                label.contains(nonFood) || nonFood == label
            }
        }

        // Match with food database for better naming
        val matchedFoods = filteredLabels.mapNotNull { (label, confidence) ->
            foodDatabase.findBestMatch(label)?.let {
                Triple(it.name, confidence * it.relevanceScore, it.category)
            }
        }

        // Apply color profile analysis to boost certain categories
        val colorAdjustedFoods = matchedFoods.map { (name, score, category) ->
            val adjustedScore = when {
                // Green foods are likely vegetables
                colorProfile.dominantColor == "green" &&
                        (category == "vegetable" || category == "salad") ->
                    score * 1.3f

                // Red foods could be fruits, tomatoes, or meat
                colorProfile.dominantColor == "red" &&
                        (category == "fruit" || category == "meat") ->
                    score * 1.2f

                // Brown/yellow foods could be bread, pastry, curry
                colorProfile.dominantColor == "yellow" &&
                        (category == "fruit" || category == "grain" || category == "dessert" || category == "curry" ) ->
                    score * 1.2f

                // Dark foods might be chocolate, coffee, etc.
                colorProfile.brightness < 0.3 &&
                        (category == "dessert" || category == "beverage") ->
                    score * 1.2f

                else -> score
            }

            Triple(name, adjustedScore, category)
        }

        // Sort by adjusted score and get the best match
        val sortedResults = colorAdjustedFoods.sortedByDescending { it.second }

        // Create alternative suggestions (different categories)
        val alternatives = sortedResults
            .distinctBy { it.third } // Take one from each category
            .take(3)
            .map { it.first }

        return if (sortedResults.isNotEmpty()) {
            val bestMatch = sortedResults.first()
            FoodDetectionResult(
                foodName = bestMatch.first.capitalize(),
                confidence = bestMatch.second,
                alternatives = alternatives
            )
        } else {
            FoodDetectionResult("", 0f, emptyList())
        }
    }

    companion object {
        // Non-food items to filter out
        private val NON_FOOD_ITEMS = setOf(
            "table", "plate", "bowl", "cup", "glass", "fork", "spoon", "knife", "napkin",
            "tablecloth", "furniture", "person", "human", "hand", "finger", "kitchen",
            "restaurant", "dining", "room", "house", "building", "wall", "floor"
        )
    }

    data class ColorProfile(val dominantColor: String, val brightness: Double)

    data class FoodDetectionResult(
        val foodName: String,
        val confidence: Float,
        val alternatives: List<String>
    )
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
