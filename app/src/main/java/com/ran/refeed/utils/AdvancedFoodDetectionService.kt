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
import com.ran.refeed.utils.FoodDatabase.MatchResult // Import MatchResult

/**
 * Advanced food detection service that combines multiple detection methods
 * for more accurate food identification.
 * Changes: Increased ML Kit threshold, denser color sampling, refined result processing
 * to combine ML Kit confidence with DB match score, reduced color boost impact,
 * added final confidence threshold.
 */
class AdvancedFoodDetectionService(private val context: Context) {
    private val TAG = "AdvancedFoodDetection"

    // Food database with common Indian and international foods
    private val foodDatabase = FoodDatabase.getInstance()

    // Confidence threshold for ML Kit labels
    private val mlKitConfidenceThreshold = 0.5f // Increased threshold

    // Minimum final confidence required to report a result
    private val finalConfidenceThreshold = 0.25f

    /**
     * Main detection method that combines multiple approaches
     */
    suspend fun detectFood(imageUri: Uri): FoodDetectionResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting advanced food detection for URI: $imageUri")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                if (bitmap == null) {
                    Log.e(TAG, "Failed to load bitmap from URI.")
                    return@withContext FoodDetectionResult.EMPTY
                }
                Log.d(TAG, "Bitmap loaded successfully (${bitmap.width}x${bitmap.height})")


                // Run multiple detection methods in parallel
                coroutineScope {
                    val mlKitLabelsDeferred = async { detectWithMlKit(bitmap) }
                    val colorAnalysisDeferred = async { analyzeColorProfile(bitmap) }

                    // Get results from all methods
                    val mlKitResults = mlKitLabelsDeferred.await()
                    val colorProfile = colorAnalysisDeferred.await()

                    Log.d(TAG, "ML Kit results count: ${mlKitResults.size}")
                    Log.d(TAG, "Color profile: Dominant=${colorProfile.dominantColor}, Brightness=${String.format("%.2f", colorProfile.brightness)}")

                    // Combine and process results
                    processResults(mlKitResults, colorProfile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during advanced food detection pipeline", e)
                FoodDetectionResult.EMPTY
            }
        }
    }

    /**
     * Detect food using ML Kit image labeling
     */
    private suspend fun detectWithMlKit(bitmap: Bitmap): List<Pair<String, Float>> =
        suspendCancellableCoroutine { continuation ->
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(mlKitConfidenceThreshold) // Use the defined threshold
                .build()

            val labeler = ImageLabeling.getClient(options)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            Log.d(TAG, "Starting ML Kit processing...")
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val results = labels.map { Pair(it.text.lowercase(), it.confidence) }
                    Log.d(TAG, "ML Kit detected ${results.size} labels: ${results.joinToString { "${it.first} (${String.format("%.2f", it.second)})" }}")
                    continuation.resume(results)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit detection failed", e)
                    continuation.resume(emptyList()) // Resume with empty list on failure
                }
                .addOnCompleteListener {
                    labeler.close() // Close labeler when done
                    Log.d(TAG, "ML Kit processing complete.")
                }
        }

    /**
     * Analyze color profile of the image to help identify food types
     */
    private suspend fun analyzeColorProfile(bitmap: Bitmap): ColorProfile {
        Log.d(TAG, "Starting color profile analysis...")
        return withContext(Dispatchers.Default) { // Use Default dispatcher for CPU-bound work
            var redSum: Long = 0
            var greenSum: Long = 0
            var blueSum: Long = 0
            var pixelCount: Long = 0
            val step = 5 // Increased sampling density

            try {
                for (x in 0 until bitmap.width step step) {
                    for (y in 0 until bitmap.height step step) {
                        val pixel = bitmap.getPixel(x, y)
                        redSum += (pixel shr 16) and 0xFF
                        greenSum += (pixel shr 8) and 0xFF
                        blueSum += pixel and 0xFF
                        pixelCount++
                    }
                }

                if (pixelCount == 0L) {
                    Log.w(TAG, "Color analysis - No pixels sampled.")
                    return@withContext ColorProfile("neutral", 0.5) // Return default if no pixels
                }

                // Calculate averages
                val avgRed = redSum.toDouble() / pixelCount
                val avgGreen = greenSum.toDouble() / pixelCount
                val avgBlue = blueSum.toDouble() / pixelCount

                // Determine dominant color (simple comparison)
                val dominantColor = when {
                    avgGreen > avgRed && avgGreen > avgBlue && avgGreen > 80 -> "green" // Added threshold
                    avgRed > avgGreen && avgRed > avgBlue && avgRed > 80 -> "red"
                    avgBlue > avgRed && avgBlue > avgGreen && avgBlue > 80 -> "blue"
                    avgRed > avgBlue && avgGreen > avgBlue && (avgRed + avgGreen) > avgBlue * 2 -> "yellow" // Simplified yellow check
                    avgRed > avgGreen && avgBlue > avgGreen && (avgRed + avgBlue) > avgGreen * 2 -> "purple" // Simplified purple check
                    (avgRed + avgGreen + avgBlue) / 3 < 70 -> "dark" // Consider dark as a pseudo-color
                    else -> "neutral" // Includes browns, beiges, greys etc.
                }

                // Calculate brightness
                val brightness = (0.299 * avgRed + 0.587 * avgGreen + 0.114 * avgBlue) / 255.0

                Log.d(TAG, "Color analysis complete: Dominant=$dominantColor, Brightness=${String.format("%.2f", brightness)}, AvgRGB=(${avgRed.toInt()}, ${avgGreen.toInt()}, ${avgBlue.toInt()})")
                ColorProfile(dominantColor, brightness)

            } catch (e: Exception) {
                Log.e(TAG, "Error during color analysis", e)
                ColorProfile("neutral", 0.5) // Return default on error
            }
        }
    }

    /**
     * Process and combine results from ML Kit and Color Profile.
     */
    private fun processResults(
        mlKitResults: List<Pair<String, Float>>,
        colorProfile: ColorProfile
    ): FoodDetectionResult {
        Log.d(TAG, "Processing ${mlKitResults.size} ML Kit results...")
        val potentialMatches = mutableListOf<ProcessedResult>()

        // 1. Filter ML Kit results and match with FoodDatabase
        mlKitResults
            .filterNot { (label, _) -> NON_FOOD_ITEMS.any { nonFood -> label.contains(nonFood) } }
            .forEach { (label, mlConfidence) ->
                val dbMatch: MatchResult? = foodDatabase.findBestMatch(label)
                if (dbMatch != null) {
                    // Initial score combines ML Kit confidence and DB match score
                    val initialScore = mlConfidence * dbMatch.score
                    potentialMatches.add(
                        ProcessedResult(
                            name = dbMatch.foodItem.name,
                            category = dbMatch.foodItem.category,
                            initialScore = initialScore,
                            matchType = dbMatch.type // Keep track of how it was matched
                        )
                    )
                    Log.d(TAG, "Matched label '$label' (ML: ${String.format("%.2f", mlConfidence)}) " +
                            "to DB item '${dbMatch.foodItem.name}' (DB Score: ${String.format("%.2f", dbMatch.score)}, Type: ${dbMatch.type}). " +
                            "Initial combined score: ${String.format("%.2f", initialScore)}")
                } else {
                    // Log unmatched labels if needed
                    // Log.d(TAG,"Label '$label' (ML: ${String.format("%.2f", mlConfidence)}) not found in DB or filtered.")
                }
            }

        if (potentialMatches.isEmpty()) {
            Log.d(TAG,"No potential food matches found after filtering and DB lookup.")
            return FoodDetectionResult.EMPTY
        }

        // 2. Apply Color Profile Adjustments
        val colorAdjustedMatches = potentialMatches.map { match ->
            val colorBoost = getColorBoost(match.category, colorProfile)
            val adjustedScore = match.initialScore * colorBoost
            Log.d(TAG, "Applying color boost to '${match.name}'. Category: ${match.category}, " +
                    "Color: ${colorProfile.dominantColor}, Brightness: ${String.format("%.2f", colorProfile.brightness)}, " +
                    "Boost: ${String.format("%.2f", colorBoost)}, Score: ${String.format("%.2f", match.initialScore)} -> ${String.format("%.2f", adjustedScore)}")
            match.copy(finalScore = adjustedScore) // Update the final score
        }

        // 3. Sort by final adjusted score and filter by confidence threshold
        val sortedResults = colorAdjustedMatches
            .sortedByDescending { it.finalScore }
            .filter { it.finalScore >= finalConfidenceThreshold }

        Log.d(TAG, "Sorted & Filtered Results (${sortedResults.size}): " +
                sortedResults.joinToString { "${it.name} (${String.format("%.2f", it.finalScore)})" })

        if (sortedResults.isEmpty()) {
            Log.d(TAG,"No matches meet the final confidence threshold ($finalConfidenceThreshold).")
            return FoodDetectionResult.EMPTY
        }

        // 4. Determine Best Match and Alternatives
        val bestMatch = sortedResults.first()

        // Create alternative suggestions (top distinct categories, excluding best match's category initially)
        val alternatives = sortedResults
            .filter { it.category != bestMatch.category } // Exclude best match's category for initial distinctness
            .distinctBy { it.category } // Get one from each distinct category
            .take(2) // Take up to 2 alternatives from different categories
            .toMutableList()

        // If not enough alternatives, add top results from the same category (if available)
        if (alternatives.size < 2) {
            sortedResults
                .filter { it.name != bestMatch.name && !alternatives.any { alt -> alt.name == it.name } } // Exclude best match and already added alts
                .take(2 - alternatives.size)
                .forEach { alternatives.add(it) }
        }

        val alternativeNames = alternatives.map { it.name.capitalizeName() }

        Log.i(TAG, "Final Detection: Best='${bestMatch.name.capitalizeName()}' (Conf: ${String.format("%.2f", bestMatch.finalScore)}), " +
                "Alts=[${alternativeNames.joinToString()}]")

        return FoodDetectionResult(
            foodName = bestMatch.name.capitalizeName(),
            confidence = bestMatch.finalScore,
            alternatives = alternativeNames
        )
    }

    /**
     * Calculates a score multiplier based on food category and image color profile.
     * Reduced boost values for less aggressive adjustment.
     */
    private fun getColorBoost(category: String, colorProfile: ColorProfile): Float {
        return when {
            // Green foods slightly boosted if image is green
            colorProfile.dominantColor == "green" &&
                    (category == "vegetable" || category == "salad" || category == "fruit") -> 1.15f // Reduced boost

            // Red foods slightly boosted if image is red
            colorProfile.dominantColor == "red" &&
                    (category == "fruit" || category == "meat" || category == "main") -> 1.1f // Reduced boost

            // Yellow/Brownish foods slightly boosted
            colorProfile.dominantColor == "yellow" &&
                    (category == "fruit" || category == "grain" || category == "dessert" || category == "curry" || category == "snack" || category == "bread") -> 1.1f // Reduced boost

            // Dark foods slightly boosted
            colorProfile.dominantColor == "dark" && // Check for 'dark' pseudo-color
                    (category == "dessert" || category == "beverage" || category == "meat") -> 1.1f // Reduced boost

            // Bright foods slightly boosted (generic)
            colorProfile.brightness > 0.6 &&
                    (category == "fruit" || category == "dessert" || category == "salad") -> 1.05f

            // Neutral - no significant boost/penalty
            else -> 1.0f
        }
    }

    // Internal data class to hold intermediate processing results
    private data class ProcessedResult(
        val name: String,
        val category: String,
        val initialScore: Float, // Score after DB match * ML confidence
        val matchType: FoodDatabase.MatchType,
        val finalScore: Float = initialScore // Score after color adjustment
    )


    companion object {
        // Expanded list of non-food items
        private val NON_FOOD_ITEMS = setOf(
            "table", "plate", "bowl", "cup", "glass", "fork", "spoon", "knife", "napkin",
            "tablecloth", "furniture", "person", "human", "hand", "finger", "kitchen", "wood",
            "restaurant", "dining", "room", "house", "building", "wall", "floor", "countertop",
            "background", "pattern", "text", "font", "circle", "rectangle", "material", "art",
            "cutlery", "tableware", "dishware", "utensil", "container", "placemat", "paper"
        )
    }

    // Data class for color analysis results
    data class ColorProfile(val dominantColor: String, val brightness: Double)

    // Data class for the final detection result
    data class FoodDetectionResult(
        val foodName: String,
        val confidence: Float,
        val alternatives: List<String>
    ) {
        companion object {
            // Represents an empty or failed detection
            val EMPTY = FoodDetectionResult("", 0f, emptyList())
        }
    }
}

// Extension function to capitalize food names appropriately
private fun String.capitalizeName(): String {
    return this.split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}