package com.ran.refeed.utils

/**
 * Database of common food items with metadata to improve detection accuracy
 * Changes: Adjusted match scoring, added differentiating keywords.
 */
class FoodDatabase private constructor() {
    private val foods = mutableListOf<FoodItem>()

    init {
        // Initialize with common food items
        // Indian foods
        addFood("Biryani", "main", 1.0f, listOf("rice", "spicy", "mixed", "indian", "curry"))
        addFood("Butter Chicken", "main", 1.0f, listOf("chicken", "curry", "gravy", "creamy", "indian", "poultry"))
        addFood("Paneer Tikka", "main", 1.0f, listOf("paneer", "cheese", "grilled", "indian", "vegetarian"))
        addFood("Chole Bhature", "main", 1.0f, listOf("chickpea", "bread", "fried", "indian", "curry"))
        addFood("Dosa", "breakfast", 1.0f, listOf("pancake", "crepe", "south indian", "rice", "lentil"))
        addFood("Idli", "breakfast", 1.0f, listOf("rice cake", "steamed", "south indian", "savory"))
        addFood("Samosa", "snack", 1.0f, listOf("pastry", "fried", "potato", "indian", "savory"))
        addFood("Gulab Jamun", "dessert", 1.0f, listOf("sweet", "fried", "syrup", "indian", "milk"))
        addFood("Jalebi", "dessert", 1.0f, listOf("sweet", "fried", "syrup", "indian", "orange", "crispy"))
        addFood("Rasgulla", "dessert", 1.0f, listOf("sweet", "cheese", "syrup", "indian", "white", "spongy"))
        addFood("Paratha", "breakfast", 1.0f, listOf("bread", "flatbread", "stuffed", "indian", "wheat"))
        addFood("Naan", "bread", 1.0f, listOf("bread", "flatbread", "tandoor", "indian", "leavened"))
        addFood("Chapati", "bread", 1.0f, listOf("bread", "flatbread", "roti", "indian", "wheat"))
        addFood("Dal", "main", 1.0f, listOf("lentil", "soup", "curry", "indian", "vegetarian"))
        addFood("Pakora", "snack", 1.0f, listOf("fritter", "fried", "vegetable", "indian", "savory"))

        addFood("Lassi", "beverage", 1.0f, listOf("yogurt", "drink", "sweet", "indian", "cold"))
        addFood("Chai", "beverage", 1.0f, listOf("tea", "milk", "spiced", "indian", "hot"))

        // International foods
        addFood("Pizza", "main", 1.0f, listOf("cheese", "tomato", "bread", "italian", "baked", "round"))
        addFood("Burger", "main", 1.0f, listOf("sandwich", "beef", "bun", "american", "patty", "meat"))
        addFood("Pasta", "main", 1.0f, listOf("noodle", "italian", "sauce", "wheat"))
        addFood("Sushi", "main", 1.0f, listOf("rice", "fish", "japanese", "raw", "seaweed", "seafood"))
        addFood("Taco", "main", 1.0f, listOf("mexican", "tortilla", "filling", "meat", "corn"))
        addFood("Salad", "side", 1.0f, listOf("vegetable", "fresh", "green", "healthy", "dressing"))
        addFood("Soup", "starter", 1.0f, listOf("liquid", "broth", "hot", "vegetable", "bowl"))
        addFood("Sandwich", "main", 1.0f, listOf("bread", "filling", "sliced", "lunch"))
        addFood("Cake", "dessert", 1.0f, listOf("sweet", "baked", "frosting", "flour", "sugar"))
        addFood("Ice Cream", "dessert", 1.0f, listOf("frozen", "sweet", "dairy", "cold", "scoop"))

        // Fruits
        addFood("Apple", "fruit", 1.0f, listOf("red", "green", "round", "fresh", "tree"))
        addFood("Banana", "fruit", 1.0f, listOf("yellow", "long", "sweet", "peel", "potassium"))
        addFood("Orange", "fruit", 1.0f, listOf("citrus", "round", "juicy", "peel", "vitamin c"))
        addFood("Mango", "fruit", 1.0f, listOf("tropical", "sweet", "yellow", "orange", "stone fruit"))
        addFood("Strawberry", "fruit", 1.0f, listOf("red", "berry", "small", "sweet", "seeds"))
        addFood("Grapes", "fruit", 1.0f, listOf("small", "cluster", "green", "purple", "vine", "sweet"))

        // Vegetables
        addFood("Tomato", "vegetable", 1.0f, listOf("red", "round", "juicy", "salad", "sauce")) // Technically a fruit, often used as vegetable
        addFood("Potato", "vegetable", 1.0f, listOf("brown", "starchy", "root", "tuber", "fries"))
        addFood("Onion", "vegetable", 1.0f, listOf("layered", "pungent", "round", "white", "red"))
        addFood("Carrot", "vegetable", 1.0f, listOf("orange", "long", "root", "crunchy"))
        addFood("Broccoli", "vegetable", 1.0f, listOf("green", "tree-like", "floret", "healthy"))
        addFood("Spinach", "vegetable", 1.0f, listOf("green", "leafy", "dark", "iron"))

        // Grains
        addFood("Rice", "grain", 1.0f, listOf("white", "brown", "grain", "staple", "boiled", "steamed"))
        addFood("Bread", "grain", 1.0f, listOf("wheat", "loaf", "baked", "sliced", "sandwich"))
        addFood("Oats", "grain", 1.0f, listOf("cereal", "breakfast", "porridge", "healthy"))

        // Protein / Meat
        addFood("Chicken", "protein", 1.0f, listOf("meat", "poultry", "white meat", "bird", "fried", "grilled", "roasted"))
        addFood("Beef", "protein", 1.0f, listOf("meat", "red meat", "cow", "steak", "mince"))
        addFood("Fish", "protein", 1.0f, listOf("seafood", "aquatic", "fillet", "scales", "gills", "ocean", "river", "white meat", "flaky"))
        addFood("Eggs", "protein", 1.0f, listOf("breakfast", "protein", "oval", "shell", "yolk", "fried", "scrambled"))
        addFood("Tofu", "protein", 1.0f, listOf("soy", "vegetarian", "bean curd", "white", "block"))
    }

    /**
     * Add a food item to the database
     */
    private fun addFood(name: String, category: String, relevanceScore: Float, keywords: List<String>) {
        foods.add(FoodItem(name.lowercase(), category, relevanceScore, keywords.map { it.lowercase() }))
    }

    /**
     * Find the best match for a detected label
     * Returns the FoodItem and the type of match which implies confidence level
     */
    fun findBestMatch(label: String): MatchResult? {
        val lowerLabel = label.lowercase()
        var bestMatch: MatchResult? = null

        for (food in foods) {
            var currentScore = 0f
            var matchType = MatchType.NONE

            // 1. Direct Name Match (Highest Priority)
            if (food.name == lowerLabel) {
                currentScore = 1.0f
                matchType = MatchType.DIRECT_NAME
            }
            // 2. Label Contains Food Name (e.g., label="delicious butter chicken", name="butter chicken")
            else if (lowerLabel.contains(food.name)) {
                // Score based on how much of the label is the name
                currentScore = food.name.length.toFloat() / lowerLabel.length.toFloat() * 0.8f // Higher score if name is large part of label
                matchType = MatchType.LABEL_CONTAINS_NAME
            }
            // 3. Food Name Contains Label (e.g., label="chicken", name="butter chicken")
            else if (food.name.contains(lowerLabel)) {
                currentScore = lowerLabel.length.toFloat() / food.name.length.toFloat() * 0.6f // Higher score if label is large part of name
                matchType = MatchType.NAME_CONTAINS_LABEL
            }

            // Check keywords only if no name match found yet or weak name match
            if (matchType == MatchType.NONE || currentScore < 0.5f) {
                for (keyword in food.keywords) {
                    if (keyword == lowerLabel) {
                        // Prioritize direct keyword match if better than weak name match
                        if (0.5f > currentScore) {
                            currentScore = 0.5f
                            matchType = MatchType.KEYWORD_DIRECT
                        }
                        break // Found best keyword match type for this food
                    } else if (lowerLabel.contains(keyword)) {
                        if (0.4f > currentScore) {
                            currentScore = 0.4f
                            matchType = MatchType.KEYWORD_LABEL_CONTAINS
                        }
                    } else if (keyword.contains(lowerLabel)) {
                        if (0.3f > currentScore) {
                            currentScore = 0.3f
                            matchType = MatchType.KEYWORD_CONTAINS_LABEL
                        }
                    }
                }
            }


            // Update best match if current food is better
            if (matchType != MatchType.NONE) {
                val finalScore = currentScore * food.relevanceScore // Combine match quality with base relevance
                if (bestMatch == null || finalScore > bestMatch.score) {
                    bestMatch = MatchResult(food, finalScore, matchType)
                }
            }
        }

        // Return the best match found across all food items
        return bestMatch
    }


    /**
     * Find all possible matches for a label, sorted by calculated score
     * (Note: This method is kept for potential future use, not directly used in the primary detection flow)
     */
    fun findAllMatches(label: String): List<FoodItem> {
        val lowerLabel = label.lowercase()
        val matches = mutableListOf<Pair<FoodItem, Float>>()

        for (food in foods) {
            var score = 0f

            // Direct name match
            if (food.name == lowerLabel) {
                score = 1.0f // Highest possible score for direct match
            } else {
                if (lowerLabel.contains(food.name)) {
                    score += food.name.length.toFloat() / lowerLabel.length.toFloat() * 0.8f
                }
                if (food.name.contains(lowerLabel)) {
                    score += lowerLabel.length.toFloat() / food.name.length.toFloat() * 0.6f
                }

                // Keyword matches (additive, but capped)
                var keywordScore = 0f
                for (keyword in food.keywords) {
                    if (keyword == lowerLabel) {
                        keywordScore = maxOf(keywordScore, 0.5f)
                    } else if (lowerLabel.contains(keyword)) {
                        keywordScore = maxOf(keywordScore, 0.3f)
                    } else if (keyword.contains(lowerLabel)) {
                        keywordScore = maxOf(keywordScore, 0.2f)
                    }
                }
                score += keywordScore
            }


            if (score > 0.1) { // Only add if there's a reasonable match score
                matches.add(Pair(food, score * food.relevanceScore))
            }
        }

        // Sort by final combined score
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
        val name: String, // Stored lowercase
        val category: String,
        val relevanceScore: Float, // Base relevance/importance of this food item
        val keywords: List<String> // Stored lowercase
    )

    // Enum to represent how a match was found
    enum class MatchType {
        DIRECT_NAME,          // label == food.name
        LABEL_CONTAINS_NAME,  // label contains food.name
        NAME_CONTAINS_LABEL,  // food.name contains label
        KEYWORD_DIRECT,       // label == keyword
        KEYWORD_LABEL_CONTAINS, // label contains keyword
        KEYWORD_CONTAINS_LABEL, // keyword contains label
        NONE
    }

    // Result wrapper for findBestMatch
    data class MatchResult(
        val foodItem: FoodItem,
        val score: Float, // Combined score (match quality * base relevance)
        val type: MatchType
    )
}