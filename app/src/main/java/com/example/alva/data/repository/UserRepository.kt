package com.example.alva.data.repository

import android.content.Context
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.database.mappers.toEntity
import com.example.alva.data.database.mappers.toModel
import com.example.alva.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                // Create JSON export for better structure and import capability
                val jsonExport = JSONObject().apply {
                    put("exportVersion", "1.0")
                    put("exportDate", dateFormat.format(Date()))
                    put("userProfile", JSONObject().apply {
                        put("id", profile.id)
                        put("name", profile.name)
                        put("email", profile.email)
                        put("age", profile.age)
                        put("height", profile.height)
                        put("weight", profile.weight)
                        put("gender", profile.gender)
                        put("activityLevel", profile.activityLevel)
                        put("calorieGoal", profile.calorieGoal)
                        put("profilePictureUri", profile.profilePictureUri ?: "")
                        put("dietaryPreferences", profile.dietaryPreferences.joinToString(","))
                        put("createdAt", dateFormat.format(profile.createdAt))
                        put("updatedAt", dateFormat.format(profile.updatedAt))
                    })
                }

                // Also create human-readable export
                val readableExport = """
                    ALVA User Profile Export
                    ========================
                    Export Date: ${dateFormat.format(Date())}
                    Export Version: 1.0
                    
                    Profile Information:
                    -------------------
                    Name: ${profile.name}
                    Email: ${profile.email}
                    Age: ${profile.age} years
                    Height: ${profile.height} cm
                    Weight: ${profile.weight} kg
                    Gender: ${profile.gender}
                    Activity Level: ${profile.activityLevel}
                    Calorie Goal: ${profile.calorieGoal} cal/day
                    Dietary Preferences: ${profile.dietaryPreferences.joinToString(", ")}
                    Profile Created: ${dateFormat.format(profile.createdAt)}
                    Last Updated: ${dateFormat.format(profile.updatedAt)}
                    
                    JSON Data (for import):
                    ----------------------
                    ${jsonExport.toString(2)}
                """.trimIndent()

                Result.success(readableExport)
            } else {
                Result.failure(Exception("No user data to export"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importUserData(importData: String): Result<UserProfile> {
        return try {
            // Try to extract JSON from the import data
            val jsonStart = importData.indexOf("{")
            val jsonEnd = importData.lastIndexOf("}") + 1

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return Result.failure(Exception("Invalid import format: No JSON data found"))
            }

            val jsonString = importData.substring(jsonStart, jsonEnd)
            val importJson = JSONObject(jsonString)

            // Validate export version
            val exportVersion = importJson.optString("exportVersion", "")
            if (exportVersion != "1.0") {
                return Result.failure(Exception("Unsupported export version: $exportVersion"))
            }

            val profileJson = importJson.getJSONObject("userProfile")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // Parse dietary preferences
            val dietaryPrefsString = profileJson.optString("dietaryPreferences", "")
            val dietaryPreferences = if (dietaryPrefsString.isNotEmpty()) {
                dietaryPrefsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }

            // Create UserProfile from imported data
            val importedProfile = UserProfile(
                id = System.currentTimeMillis(), // Generate new ID
                name = profileJson.getString("name"),
                email = profileJson.getString("email"),
                age = profileJson.getInt("age"),
                height = profileJson.getDouble("height").toFloat(),
                weight = profileJson.getDouble("weight").toFloat(),
                gender = profileJson.optString("gender", "Not specified"),
                activityLevel = profileJson.optString("activityLevel", "Moderate"),
                calorieGoal = profileJson.optInt("calorieGoal", 2000),
                profilePictureUri = profileJson.optString("profilePictureUri").takeIf { it.isNotEmpty() },
                dietaryPreferences = dietaryPreferences,
                createdAt = try {
                    dateFormat.parse(profileJson.getString("createdAt")) ?: Date()
                } catch (e: Exception) {
                    Date()
                },
                updatedAt = Date() // Set current date as updated date
            )

            // Validate imported data
            if (importedProfile.name.isEmpty() || importedProfile.email.isEmpty()) {
                return Result.failure(Exception("Invalid profile data: Name and email are required"))
            }

            if (importedProfile.age <= 0 || importedProfile.height <= 0 || importedProfile.weight <= 0) {
                return Result.failure(Exception("Invalid profile data: Age, height, and weight must be positive"))
            }

            // Save the imported profile
            updateUserProfile(importedProfile)

            Result.success(importedProfile)
        } catch (e: Exception) {
            Result.failure(Exception("Import failed: ${e.message}"))
        }
    }

    suspend fun validateUserData(profile: UserProfile): Result<UserProfile> {
        return try {
            val errors = mutableListOf<String>()

            if (profile.name.isBlank()) {
                errors.add("Name cannot be empty")
            }

            if (profile.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(profile.email).matches()) {
                errors.add("Valid email is required")
            }

            if (profile.age <= 0 || profile.age > 150) {
                errors.add("Age must be between 1 and 150")
            }

            if (profile.height <= 0 || profile.height > 300) {
                errors.add("Height must be between 1 and 300 cm")
            }

            if (profile.weight <= 0 || profile.weight > 500) {
                errors.add("Weight must be between 1 and 500 kg")
            }

            if (profile.calorieGoal <= 0 || profile.calorieGoal > 10000) {
                errors.add("Calorie goal must be between 1 and 10000")
            }

            if (errors.isNotEmpty()) {
                Result.failure(Exception("Validation errors: ${errors.joinToString(", ")}"))
            } else {
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun backupUserData(): Result<String> {
        return try {
            val profile = getUserProfile()
            if (profile != null) {
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val backupFileName = "alva_backup_${dateFormat.format(Date())}.json"

                val backupData = JSONObject().apply {
                    put("backupVersion", "1.0")
                    put("backupDate", dateFormat.format(Date()))
                    put("appVersion", "1.0") // You can get this from BuildConfig
                    put("userProfile", JSONObject().apply {
                        put("name", profile.name)
                        put("email", profile.email)
                        put("age", profile.age)
                        put("height", profile.height)
                        put("weight", profile.weight)
                        put("gender", profile.gender)
                        put("activityLevel", profile.activityLevel)
                        put("calorieGoal", profile.calorieGoal)
                        put("dietaryPreferences", profile.dietaryPreferences.joinToString(","))
                    })
                }

                Result.success(backupData.toString())
            } else {
                Result.failure(Exception("No user data to backup"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}