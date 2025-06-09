package com.example.alva.data.models

data class FoodInfo(
    val name: String,
    val estimatedCalories: Int,
    val confidence: Float = 0f,
    val alternatives: List<String> = emptyList(),
    val nutritionInfo: NutritionInfo? = null
)

data class NutritionInfo(
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float
)