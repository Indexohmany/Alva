package com.example.alva.data.models

data class ProductInfo(
    val barcode: String,
    val name: String,
    val calories: Int,
    val brand: String = "",
    val servingSize: String = "",
    val nutritionInfo: NutritionInfo? = null
)