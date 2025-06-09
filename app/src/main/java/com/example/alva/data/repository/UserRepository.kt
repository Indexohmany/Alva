package com.example.alva.data.repository

import android.content.Context
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.database.mappers.toEntity
import com.example.alva.data.database.mappers.toModel
import com.example.alva.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class UserRepository(context: Context) {

    private val databaseModule = DatabaseModule.getInstance(context)
    private val userProfileDao = databaseModule.userProfileDao

    suspend fun getUserProfile(): UserProfile? {
        return userProfileDao.getCurrentUserProfile()?.toModel()
    }

    fun getUserProfileFlow(): Flow<UserProfile?> {
        return userProfileDao.getCurrentUserProfileFlow().map { it?.toModel() }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val updatedProfile = profile.copy(updatedAt = Date())
        userProfileDao.insertUserProfile(updatedProfile.toEntity())
    }

    suspend fun createUserProfile(profile: UserProfile) {
        val newProfile = profile.copy(
            id = System.currentTimeMillis(),
            createdAt = Date(),
            updatedAt = Date()
        )
        userProfileDao.insertUserProfile(newProfile.toEntity())
    }

    suspend fun clearUserData() {
        userProfileDao.deleteAllProfiles()
    }

    suspend fun exportUserData(): Result<String> {
        return try {
            val profile = getUserProfile()
            if (profile != null) {
                val exportData = """
                    User Profile Export
                    ==================
                    Name: ${profile.name}
                    Email: ${profile.email}
                    Age: ${profile.age}
                    Height: ${profile.height}cm
                    Weight: ${profile.weight}kg
                    Calorie Goal: ${profile.calorieGoal}
                    Activity Level: ${profile.activityLevel}
                    Dietary Preferences: ${profile.dietaryPreferences.joinToString(", ")}
                    Created: ${profile.createdAt}
                    Last Updated: ${profile.updatedAt}
                """.trimIndent()
                Result.success(exportData)
            } else {
                Result.failure(Exception("No user data to export"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}