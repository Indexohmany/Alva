package com.example.alva.data.models

import java.util.Date

data class UserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val height: Float, // in cm
    val weight: Float, // in kg
    val gender: String = "Not specified",
    val activityLevel: String = "Moderate",
    val calorieGoal: Int = 2000,
    val profilePictureUri: String? = null,
    val dietaryPreferences: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)