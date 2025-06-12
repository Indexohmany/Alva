package com.example.alva.ui.profile

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alva.data.models.UserProfile
import com.example.alva.data.models.WeeklyStats
import com.example.alva.data.models.MonthlyStats
import com.example.alva.data.repository.UserRepository
import com.example.alva.data.repository.CalorieRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application.applicationContext)
    private val calorieRepository = CalorieRepository(application.applicationContext)

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _weeklyStats = MutableLiveData<WeeklyStats?>()
    val weeklyStats: LiveData<WeeklyStats?> = _weeklyStats

    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _exportFileUri = MutableLiveData<Uri?>()
    val exportFileUri: LiveData<Uri?> = _exportFileUri

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val profile = userRepository.getUserProfile()
                _userProfile.value = profile

                if (profile != null) {
                    loadWeeklyStats()
                    loadMonthlyStats()
                } else {
                    _weeklyStats.value = null
                    _monthlyStats.value = null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load profile data", e)
                _errorMessage.value = "Failed to load profile data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadWeeklyStats() {
        try {
            val weeklyData = calorieRepository.getWeeklyCalories()
            val profile = _userProfile.value
            val goalCalories = profile?.calorieGoal ?: 2000

            if (weeklyData.isEmpty()) {
                _weeklyStats.value = WeeklyStats(
                    averageCalories = 0,
                    daysGoalMet = 0,
                    totalCalories = 0,
                    bestDay = "No data",
                    bestDayCalories = 0,
                    worstDay = "No data",
                    worstDayCalories = 0
                )
                return
            }

            val averageCalories = weeklyData.average().toInt()
            val goalTolerance = 0.1
            val minGoal = (goalCalories * (1 - goalTolerance)).toInt()
            val maxGoal = (goalCalories * (1 + goalTolerance)).toInt()

            val daysGoalMet = weeklyData.count { it in minGoal..maxGoal }
            val totalCalories = weeklyData.sum()

            val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val bestDayIndex = weeklyData.indices.maxByOrNull { weeklyData[it] } ?: 0
            val worstDayIndex = weeklyData.indices.minByOrNull { weeklyData[it] } ?: 0

            _weeklyStats.value = WeeklyStats(
                averageCalories = averageCalories,
                daysGoalMet = daysGoalMet,
                totalCalories = totalCalories,
                bestDay = daysOfWeek.getOrNull(bestDayIndex) ?: "Unknown",
                bestDayCalories = weeklyData.getOrNull(bestDayIndex) ?: 0,
                worstDay = daysOfWeek.getOrNull(worstDayIndex) ?: "Unknown",
                worstDayCalories = weeklyData.getOrNull(worstDayIndex) ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load weekly statistics", e)
            _errorMessage.value = "Failed to load weekly statistics: ${e.message}"
            _weeklyStats.value = null
        }
    }

    private suspend fun loadMonthlyStats() {
        try {
            val monthlyData = calorieRepository.getMonthlyCalories()
            val profile = _userProfile.value
            val goalCalories = profile?.calorieGoal ?: 2000

            if (monthlyData.isEmpty()) {
                _monthlyStats.value = MonthlyStats(
                    averageCalories = 0,
                    daysGoalMet = 0,
                    totalDays = 0,
                    totalCalories = 0,
                    bestWeek = 0,
                    bestWeekCalories = 0,
                    worstWeek = 0,
                    worstWeekCalories = 0
                )
                return
            }

            val averageCalories = monthlyData.average().toInt()
            val goalTolerance = 0.1
            val minGoal = (goalCalories * (1 - goalTolerance)).toInt()
            val maxGoal = (goalCalories * (1 + goalTolerance)).toInt()

            val daysGoalMet = monthlyData.count { it in minGoal..maxGoal }
            val totalCalories = monthlyData.sum()

            val weeklyAverages = monthlyData.chunked(7).mapIndexed { index, week ->
                if (week.isNotEmpty()) week.average().toInt() else 0
            }

            val bestWeekIndex = weeklyAverages.indices.maxByOrNull { weeklyAverages[it] } ?: 0
            val worstWeekIndex = weeklyAverages.indices.minByOrNull { weeklyAverages[it] } ?: 0

            _monthlyStats.value = MonthlyStats(
                averageCalories = averageCalories,
                daysGoalMet = daysGoalMet,
                totalDays = monthlyData.size,
                totalCalories = totalCalories,
                bestWeek = bestWeekIndex + 1,
                bestWeekCalories = weeklyAverages.getOrNull(bestWeekIndex) ?: 0,
                worstWeek = worstWeekIndex + 1,
                worstWeekCalories = weeklyAverages.getOrNull(worstWeekIndex) ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load monthly statistics", e)
            _errorMessage.value = "Failed to load monthly statistics: ${e.message}"
            _monthlyStats.value = null
        }
    }

    fun updateProfile(name: String, email: String, age: Int, height: Float, weight: Float, gender: String = "Not specified") {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentProfile = _userProfile.value
                val updatedProfile = currentProfile?.copy(
                    name = name,
                    email = email,
                    age = age,
                    height = height,
                    weight = weight,
                    gender = gender,
                    updatedAt = Date()
                ) ?: UserProfile(
                    id = System.currentTimeMillis(),
                    name = name,
                    email = email,
                    age = age,
                    height = height,
                    weight = weight,
                    gender = gender,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                userRepository.updateUserProfile(updatedProfile)
                _userProfile.value = updatedProfile
                _successMessage.value = "Profile updated successfully"

                loadWeeklyStats()
                loadMonthlyStats()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to update profile", e)
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateActivityLevel(activityLevel: String) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfile.value
                currentProfile?.let { profile ->
                    val updatedProfile = profile.copy(
                        activityLevel = activityLevel,
                        updatedAt = Date()
                    )
                    userRepository.updateUserProfile(updatedProfile)
                    _userProfile.value = updatedProfile
                    _successMessage.value = "Activity level updated to $activityLevel"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update activity level", e)
                _errorMessage.value = "Failed to update activity level: ${e.message}"
            }
        }
    }

    fun updateProfileAvatar(avatarId: Int?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentProfile = _userProfile.value
                if (currentProfile == null) {
                    _errorMessage.value = "No profile found to update"
                    return@launch
                }

                val updatedProfile = currentProfile.copy(
                    profilePictureUri = avatarId?.toString(),
                    updatedAt = Date()
                )

                userRepository.updateUserProfile(updatedProfile)
                _userProfile.value = updatedProfile

                when {
                    avatarId == null -> _successMessage.value = "Avatar removed"
                    else -> _successMessage.value = "Avatar updated successfully"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to update avatar", e)
                _errorMessage.value = "Failed to update avatar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentAvatarId(): Int? {
        return _userProfile.value?.profilePictureUri?.toIntOrNull()
    }

    fun updateCalorieGoal(newGoal: Int) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfile.value
                currentProfile?.let { profile ->
                    val updatedProfile = profile.copy(
                        calorieGoal = newGoal,
                        updatedAt = Date()
                    )
                    userRepository.updateUserProfile(updatedProfile)
                    _userProfile.value = updatedProfile

                    loadWeeklyStats()
                    loadMonthlyStats()

                    _successMessage.value = "Calorie goal updated to $newGoal"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update calorie goal", e)
                _errorMessage.value = "Failed to update calorie goal: ${e.message}"
            }
        }
    }

    fun exportUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val exportResult = userRepository.exportUserData()
                if (exportResult.isSuccess) {
                    val exportData = exportResult.getOrNull()
                    if (exportData != null) {
                        val success = saveToDownloads(exportData)
                        if (success) {
                            _successMessage.value = "Data exported successfully to Downloads folder!"
                        } else {
                            val file = createExportFile(exportData)
                            if (file != null) {
                                _successMessage.value = "Export file created in app directory"
                            } else {
                                _errorMessage.value = "Failed to create export file"
                            }
                        }
                    } else {
                        _errorMessage.value = "No data to export"
                    }
                } else {
                    _errorMessage.value = exportResult.exceptionOrNull()?.message ?: "Failed to export data"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
                _errorMessage.value = "Export failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveToDownloads(exportData: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "alva_export_$timestamp.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = getApplication<Application>().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { fileUri ->
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(exportData.toByteArray())
                    }
                    true
                } ?: false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                FileWriter(file).use { writer ->
                    writer.write(exportData)
                }

                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to downloads", e)
            false
        }
    }

    private fun createExportFile(exportData: String): File? {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "alva_export_$timestamp.txt"

            val externalDir = getApplication<Application>().getExternalFilesDir(null)
            val exportFile = File(externalDir, fileName)

            FileWriter(exportFile).use { writer ->
                writer.write(exportData)
            }

            exportFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create export file", e)
            null
        }
    }

    fun importUserData(importData: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val importResult = userRepository.importUserData(importData)
                if (importResult.isSuccess) {
                    _successMessage.value = "Data imported successfully"
                    loadUserData()
                } else {
                    _errorMessage.value = importResult.exceptionOrNull()?.message ?: "Failed to import data"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _errorMessage.value = "Import failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.clearUserData()
                _userProfile.value = null
                _weeklyStats.value = null
                _monthlyStats.value = null
                _successMessage.value = "Logged out successfully"
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed", e)
                _errorMessage.value = "Logout failed: ${e.message}"
            }
        }
    }

    fun calculateBMI(): Float {
        val profile = _userProfile.value
        return if (profile != null && profile.height > 0 && profile.weight > 0) {
            val heightInMeters = profile.height / 100
            profile.weight / (heightInMeters * heightInMeters)
        } else {
            0f
        }
    }

    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi == 0f -> "No data"
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun getRecommendedCalories(): Int {
        val profile = _userProfile.value ?: return 2000

        val bmr = if (profile.gender.equals("Male", true)) {
            (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) + 5
        } else {
            (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) - 161
        }

        val activityFactor = when (profile.activityLevel) {
            "Sedentary" -> 1.2
            "Light" -> 1.375
            "Moderate" -> 1.55
            "Active" -> 1.725
            "Very Active" -> 1.9
            else -> 1.55
        }

        return (bmr * activityFactor).toInt()
    }

    fun refreshStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                loadWeeklyStats()
                loadMonthlyStats()
                _successMessage.value = "Statistics refreshed"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh statistics", e)
                _errorMessage.value = "Failed to refresh statistics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun clearExportUri() {
        _exportFileUri.value = null
    }
}