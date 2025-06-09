package com.example.alva.data.models

import java.util.Date

data class FoodEntry(
    val id: Long = 0L,
    val name: String,
    val calories: Int,
    val timestamp: Date = Date(),
    val source: String = "Manual", // Manual, Camera Recognition, Barcode Scan
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val quantity: Float = 1f,
    val unit: String = "serving"
)