package com.example.alva.data.database.mappers

import com.example.alva.data.database.entities.*
import com.example.alva.data.models.*

// Extension functions to convert between entities and models

// UserProfile mappers
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        email = email,
        age = age,
        height = height,
        weight = weight,
        gender = gender,
        activityLevel = activityLevel,
        calorieGoal = calorieGoal,
        profilePictureUri = profilePictureUri,
        dietaryPreferences = dietaryPreferences,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfileEntity.toModel(): UserProfile {
    return UserProfile(
        id = id,
        name = name,
        email = email,
        age = age,
        height = height,
        weight = weight,
        gender = gender,
        activityLevel = activityLevel,
        calorieGoal = calorieGoal,
        profilePictureUri = profilePictureUri,
        dietaryPreferences = dietaryPreferences,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// FoodEntry mappers
fun FoodEntry.toEntity(): FoodEntryEntity {
    return FoodEntryEntity(
        id = id,
        name = name,
        calories = calories,
        timestamp = timestamp,
        source = source,
        protein = protein,
        carbs = carbs,
        fat = fat,
        quantity = quantity,
        unit = unit
    )
}

fun FoodEntryEntity.toModel(): FoodEntry {
    return FoodEntry(
        id = id,
        name = name,
        calories = calories,
        timestamp = timestamp,
        source = source,
        protein = protein,
        carbs = carbs,
        fat = fat,
        quantity = quantity,
        unit = unit
    )
}

// ChatMessage mappers
fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        content = content,
        type = type.name,
        timestamp = timestamp
    )
}

fun ChatMessageEntity.toModel(): ChatMessage {
    return ChatMessage(
        id = id,
        content = content,
        type = MessageType.valueOf(type),
        timestamp = timestamp
    )
}

// ProductInfo mappers
fun ProductInfo.toEntity(): ProductInfoEntity {
    return ProductInfoEntity(
        barcode = barcode,
        name = name,
        calories = calories,
        brand = brand,
        servingSize = servingSize,
        protein = nutritionInfo?.protein ?: 0f,
        carbs = nutritionInfo?.carbs ?: 0f,
        fat = nutritionInfo?.fat ?: 0f,
        fiber = nutritionInfo?.fiber ?: 0f,
        sugar = nutritionInfo?.sugar ?: 0f,
        sodium = nutritionInfo?.sodium ?: 0f
    )
}

fun ProductInfoEntity.toModel(): ProductInfo {
    return ProductInfo(
        barcode = barcode,
        name = name,
        calories = calories,
        brand = brand,
        servingSize = servingSize,
        nutritionInfo = NutritionInfo(
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium
        )
    )
}