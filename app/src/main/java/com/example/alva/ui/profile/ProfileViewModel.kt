package com.example.alva.ui.profile

import android.app.Application
import android.net.Uri
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

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application.applicationContext)
    private val calorieRepository = CalorieRepository(application.applicationContext)

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _weeklyStats = MutableLiveData<WeeklyStats>()
    val weeklyStats: LiveData<WeeklyStats> = _weeklyStats

    private val _monthlyStats = MutableLiveData<MonthlyStats>()
    val monthlyStats: LiveData<MonthlyStats> = _monthlyStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Load user profile
                val profile = userRepository.getUserProfile()
                _userProfile.value = profile

                // Load statistics
                loadWeeklyStats()
                loadMonthlyStats()

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadWeeklyStats() {
        val weeklyData = calorieRepository.getWeeklyCalories()
        val profile = _userProfile.value
        val goalCalories = profile?.calorieGoal ?: 2000

        val averageCalories = if (weeklyData.isNotEmpty()) {
            weeklyData.average().toInt()
        } else 0

        val daysGoalMet = weeklyData.count { it >= goalCalories * 0.9 && it <= goalCalories * 1.1 }
        val totalCalories = weeklyData.sum()

        // Calculate best and worst days
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val bestDayIndex = weeklyData.indices.maxByOrNull { weeklyData[it] } ?: 0
        val worstDayIndex = weeklyData.indices.minByOrNull { weeklyData[it] } ?: 0

        _weeklyStats.value = WeeklyStats(
            averageCalories = averageCalories,
            daysGoalMet = daysGoalMet,
            totalCalories = totalCalories,
            bestDay = daysOfWeek[bestDayIndex],
            bestDayCalories = weeklyData[bestDayIndex],
            worstDay = daysOfWeek[worstDayIndex],
            worstDayCalories = weeklyData[worstDayIndex]
        )
    }

    private suspend fun loadMonthlyStats() {
        val monthlyData = calorieRepository.getMonthlyCalories()
        val profile = _userProfile.value
        val goalCalories = profile?.calorieGoal ?: 2000

        val averageCalories = if (monthlyData.isNotEmpty()) {
            monthlyData.average().toInt()
        } else 0

        val daysGoalMet = monthlyData.count { it >= goalCalories * 0.9 && it <= goalCalories * 1.1 }
        val totalCalories = monthlyData.sum()

        // Calculate best and worst weeks
        val weeklyAverages = monthlyData.chunked(7).map { it.average().toInt() }
        val bestWeekIndex = weeklyAverages.indices.maxByOrNull { weeklyAverages[it] } ?: 0
        val worstWeekIndex = weeklyAverages.indices.minByOrNull { weeklyAverages[it] } ?: 0

        _monthlyStats.value = MonthlyStats(
            averageCalories = averageCalories,
            daysGoalMet = daysGoalMet,
            totalDays = monthlyData.size,
            totalCalories = totalCalories,
            bestWeek = bestWeekIndex + 1,
            bestWeekCalories = weeklyAverages[bestWeekIndex],
            worstWeek = worstWeekIndex + 1,
            worstWeekCalories = weeklyAverages[worstWeekIndex]
        )
    }

    fun updateProfile(name: String, email: String, age: Int, height: Float, weight: Float) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentProfile = _userProfile.value
                val updatedProfile = currentProfile?.copy(
                    name = name,
                    email = email,
                    age = age,
                    height = height,
                    weight = weight
                ) ?: UserProfile(
                    id = System.currentTimeMillis(),
                    name = name,
                    email = email,
                    age = age,
                    height = height,
                    weight = weight
                )

                userRepository.updateUserProfile(updatedProfile)
                _userProfile.value = updatedProfile
                _successMessage.value = "Profile updated successfully"

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentProfile = _userProfile.value
                currentProfile?.let { profile ->
                    val updatedProfile = profile.copy(profilePictureUri = imageUri.toString())
                    userRepository.updateUserProfile(updatedProfile)
                    _userProfile.value = updatedProfile
                    _successMessage.value = "Profile picture updated"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile picture"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCalorieGoal(newGoal: Int) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfile.value
                currentProfile?.let { profile ->
                    val updatedProfile = profile.copy(calorieGoal = newGoal)
                    userRepository.updateUserProfile(updatedProfile)
                    _userProfile.value = updatedProfile

                    // Refresh stats with new goal
                    loadWeeklyStats()
                    loadMonthlyStats()

                    _successMessage.value = "Calorie goal updated to $newGoal"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update calorie goal"
            }
        }
    }

    fun exportUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val exportResult = userRepository.exportUserData()
                if (exportResult.isSuccess) {
                    _successMessage.value = "Data exported successfully"
                } else {
                    _errorMessage.value = "Failed to export data"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Export failed: ${e.message}"
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
                _successMessage.value = "Logged out successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Logout failed"
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
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun getRecommendedCalories(): Int {
        val profile = _userProfile.value ?: return 2000

        // Harris-Benedict Equation
        val bmr = if (profile.gender == "Male") {
            88.362 + (13.397 * profile.weight) + (4.799 * profile.height) - (5.677 * profile.age)
        } else {
            447.593 + (9.247 * profile.weight) + (3.098 * profile.height) - (4.330 * profile.age)
        }

        // Activity factor
        val activityFactor = when (profile.activityLevel) {
            "Sedentary" -> 1.2
            "Light" -> 1.375
            "Moderate" -> 1.55
            "Active" -> 1.725
            "Very Active" -> 1.9
            else -> 1.2
        }

        return (bmr * activityFactor).toInt()
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}