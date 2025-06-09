package com.example.alva.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_info")
data class ProductInfoEntity(
    @PrimaryKey
    val barcode: String,
    val name: String,
    val calories: Int,
    val brand: String = "",
    val servingSize: String = "",
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)