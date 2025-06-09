package com.example.alva.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.alva.data.database.converters.DateConverter
import com.example.alva.data.database.converters.StringListConverter
import java.util.Date

@Entity(tableName = "user_profiles")
@TypeConverters(DateConverter::class, StringListConverter::class)
data class UserProfileEntity(
    @PrimaryKey
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