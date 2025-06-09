package com.example.alva.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.alva.data.database.converters.DateConverter
import java.util.Date

@Entity(tableName = "food_entries")
@TypeConverters(DateConverter::class)
data class FoodEntryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val calories: Int,
    val timestamp: Date,
    val source: String = "Manual", // Manual, Camera Recognition, Barcode Scan
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val quantity: Float = 1f,
    val unit: String = "serving"
)