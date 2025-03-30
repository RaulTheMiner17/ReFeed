package com.ran.refeed.utils

/**
 * Database of common food items with metadata to improve detection accuracy
 */
class FoodDatabase private constructor() {
    private val foods = mutableListOf<FoodItem>()

    init {
        // Initialize with common food items
        // Indian foods
        addFood("Biryani", "main", 1.0f, listOf("rice", "spicy", "mixed"))
        addFood("Butter Chicken", "main", 1.0f, listOf("chicken", "curry", "gravy"))
        addFood("Paneer Tikka", "main", 1.0f, listOf("paneer", "cheese", "grilled"))
        addFood("Chole Bhature", "main", 1.0f, listOf("chickpea", "bread", "fried"))
        addFood("Dosa", "breakfast", 1.0f, listOf("pancake", "crepe", "south indian"))
        addFood("Idli", "breakfast", 1.0f, listOf("rice cake", "steamed", "south indian"))
        addFood("Samosa", "snack", 1.0f, listOf("pastry", "fried", "potato"))
        addFood("Gulab Jamun", "dessert", 1.0f, listOf("sweet", "fried", "syrup"))
        addFood("Jalebi", "dessert", 1.0f, listOf("sweet", "fried", "syrup"))
        addFood("Rasgulla", "dessert", 1.0f, listOf("sweet", "cheese", "syrup"))
        addFood("Paratha", "breakfast", 1.0f, listOf("bread", "flatbread", "stuffed"))
        addFood("Naan", "bread", 1.0f, listOf("bread", "flatbread", "tandoor"))
        addFood("Chapati", "bread", 1.0f, listOf("bread", "flatbread", "roti"))
        addFood("Dal", "main", 1.0f, listOf("lentil", "soup", "curry"))
        addFood("Pakora", "snack", 1.0f, listOf("fritter", "fried", "vegetable"))

        addFood("Lassi", "beverage", 1.0f, listOf("yogurt", "drink", "sweet"))
        addFood("Chai", "beverage", 1.0f, listOf("tea", "milk", "spiced"))

        // International foods
        addFood("Pizza", "main", 1.0f, listOf("cheese", "tomato", "bread", "italian"))
        addFood("Burger", "main", 1.0f, listOf("sandwich", "beef", "bun"))
        addFood("Pasta", "main", 1.0f, listOf("noodle", "italian", "sauce"))
        addFood("Sushi", "main", 1.0f, listOf("rice", "fish", "japanese", "raw"))
        addFood("Taco", "main", 1.0f, listOf("mexican", "tortilla", "filling"))
        addFood("Salad", "side", 1.0f, listOf("vegetable", "fresh", "green"))
        addFood("Soup", "starter", 1.0f, listOf("liquid", "broth", "hot"))
        addFood("Sandwich", "main", 1.0f, listOf("bread", "filling", "sliced"))
        addFood("Cake", "dessert", 1.0f, listOf("sweet", "baked", "frosting"))
        addFood("Ice Cream", "dessert", 1.0f, listOf("frozen", "sweet", "dairy"))

        // Fruits
        addFood("Apple", "fruit", 1.0f, listOf("red", "green", "round", "fresh"))
        addFood("Banana", "fruit", 1.0f, listOf("yellow", "long", "sweet"))
        addFood("Orange", "fruit", 1.0f, listOf("citrus", "round", "juicy"))
        addFood("Mango", "fruit", 1.0f, listOf("tropical", "sweet", "yellow"))
        addFood("Strawberry", "fruit", 1.0f, listOf("red", "berry", "small"))
        addFood("Grapes", "fruit", 1.0f, listOf("small", "cluster", "green", "purple"))

        // Vegetables
        addFood("Tomato", "vegetable", 1.0f, listOf("red", "round", "juicy"))
        addFood("Potato", "vegetable", 1.0f, listOf("brown", "starchy", "root"))
        addFood("Onion", "vegetable", 1.0f, listOf("layered", "pungent", "round"))
        addFood("Carrot", "vegetable", 1.0f, listOf("orange", "long", "root"))
        addFood("Broccoli", "vegetable", 1.0f, listOf("green", "tree-like", "floret"))
        addFood("Spinach", "vegetable", 1.0f, listOf("green", "leafy", "dark"))

        // Grains
        addFood("Rice", "grain", 1.0f, listOf("white", "grain", "staple"))
        addFood("Bread", "grain", 1.0f, listOf("wheat", "loaf", "baked"))
        addFood("Oats", "grain", 1.0f, listOf("cereal", "breakfast", "porridge"))

        // Protein
        addFood("Chicken", "protein", 1.0f, listOf("meat", "poultry", "white meat"))
        addFood("Beef", "protein", 1.0f, listOf("meat", "red meat", "cow"))
        addFood("Fish", "protein", 1.0f, listOf("seafood", "aquatic", "fillet"))
        addFood("Eggs", "protein", 1.0f, listOf("breakfast", "protein", "oval"))
        addFood("Tofu", "protein", 1.0f, listOf("soy", "vegetarian", "bean curd"))
    }

    /**
     * Add a food item to the database
     */
    private fun addFood(name: String, category: String, relevanceScore: Float, keywords: List<String>) {
        foods.add(FoodItem(name, category, relevanceScore, keywords))
    }

    /**
     * Find the best match for a detected label
     */
    fun findBestMatch(label: String): FoodItem? {
        val lowerLabel = label.lowercase()

        // Direct match with name
        val directMatch = foods.find { it.name.equals(lowerLabel, ignoreCase = true) }
        if (directMatch != null) {
            return directMatch.copy(relevanceScore = directMatch.relevanceScore * 1.5f)
        }

        // Check if label contains food name
        val containsMatch = foods.find { lowerLabel.contains(it.name.lowercase()) }
        if (containsMatch != null) {
            return containsMatch.copy(relevanceScore = containsMatch.relevanceScore * 1.2f)
        }

        // Check if food name contains label
        val reversedContainsMatch = foods.find { it.name.lowercase().contains(lowerLabel) }
        if (reversedContainsMatch != null) {
            return reversedContainsMatch.copy(relevanceScore = reversedContainsMatch.relevanceScore * 1.1f)
        }

        // Check keywords
        val keywordMatches = foods.filter { food ->
            food.keywords.any { keyword ->
                keyword.equals(lowerLabel, ignoreCase = true) ||
                        lowerLabel.contains(keyword) ||
                        keyword.contains(lowerLabel)
            }
        }

        if (keywordMatches.isNotEmpty()) {
            // Return the food with the highest relevance score
            return keywordMatches.maxByOrNull { it.relevanceScore }
        }

        return null
    }

    /**
     * Find all possible matches for a label, sorted by relevance
     */
    fun findAllMatches(label: String): List<FoodItem> {
        val lowerLabel = label.lowercase()
        val matches = mutableListOf<Pair<FoodItem, Float>>()

        for (food in foods) {
            var score = 0f

            // Direct name match
            if (food.name.equals(lowerLabel, ignoreCase = true)) {
                score += 1.0f
            } else if (lowerLabel.contains(food.name.lowercase())) {
                score += 0.8f
            } else if (food.name.lowercase().contains(lowerLabel)) {
                score += 0.6f
            }

            // Keyword matches
            for (keyword in food.keywords) {
                if (keyword.equals(lowerLabel, ignoreCase = true)) {
                    score += 0.5f
                } else if (lowerLabel.contains(keyword)) {
                    score += 0.3f
                } else if (keyword.contains(lowerLabel)) {
                    score += 0.2f
                }
            }

            if (score > 0) {
                matches.add(Pair(food, score * food.relevanceScore))
            }
        }

        return matches.sortedByDescending { it.second }.map { it.first }
    }

    companion object {
        @Volatile
        private var instance: FoodDatabase? = null

        fun getInstance(): FoodDatabase {
            return instance ?: synchronized(this) {
                instance ?: FoodDatabase().also { instance = it }
            }
        }
    }

    data class FoodItem(
        val name: String,
        val category: String,
        val relevanceScore: Float,
        val keywords: List<String>
    )
}
