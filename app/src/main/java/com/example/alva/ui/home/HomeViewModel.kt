package com.example.alva.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alva.data.models.FoodEntry
import com.example.alva.data.repository.CalorieRepository
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalorieRepository(application.applicationContext)

    private val _dailyCalories = MutableLiveData<Int>().apply { value = 0 }
    val dailyCalories: LiveData<Int> = _dailyCalories

    private val _calorieGoal = MutableLiveData<Int>().apply { value = 2000 }
    val calorieGoal: LiveData<Int> = _calorieGoal

    private val _foodEntries = MutableLiveData<List<FoodEntry>>().apply { value = emptyList() }
    val foodEntries: LiveData<List<FoodEntry>> = _foodEntries

    private val _remainingCalories = MutableLiveData<Int>()
    val remainingCalories: LiveData<Int> = _remainingCalories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Load today's food entries
                val todayEntries = repository.getTodayFoodEntries()
                _foodEntries.value = todayEntries

                // Calculate total calories
                val totalCalories = todayEntries.sumOf { it.calories }
                _dailyCalories.value = totalCalories

                // Load calorie goal
                val goal = repository.getCalorieGoal()
                _calorieGoal.value = goal

                // Calculate remaining calories
                val remaining = goal - totalCalories
                _remainingCalories.value = remaining

            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFoodEntry(foodName: String, calories: Int) {
        viewModelScope.launch {
            try {
                val newEntry = FoodEntry(
                    id = System.currentTimeMillis(),
                    name = foodName,
                    calories = calories,
                    timestamp = Date()
                )

                repository.addFoodEntry(newEntry)
                loadTodayData() // Refresh data
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun deleteFoodEntry(foodEntry: FoodEntry) {
        viewModelScope.launch {
            try {
                repository.deleteFoodEntry(foodEntry.id)
                loadTodayData() // Refresh data
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun updateCalorieGoal(newGoal: Int) {
        viewModelScope.launch {
            try {
                _calorieGoal.value = newGoal
                repository.updateCalorieGoal(newGoal)

                // Recalculate remaining calories
                val remaining = newGoal - (_dailyCalories.value ?: 0)
                _remainingCalories.value = remaining
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun getWeeklyCalories(): List<Int> {
        // This should also be made suspend, but for now return empty list
        // You can call this from a coroutine in your fragment/activity
        return emptyList()
    }

    fun loadWeeklyCalories(callback: (List<Int>) -> Unit) {
        viewModelScope.launch {
            try {
                val weeklyData = repository.getWeeklyCalories()
                callback(weeklyData)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    fun getMonthlyCalories(): List<Int> {
        // This should also be made suspend, but for now return empty list
        // You can call this from a coroutine in your fragment/activity
        return emptyList()
    }

    fun loadMonthlyCalories(callback: (List<Int>) -> Unit) {
        viewModelScope.launch {
            try {
                val monthlyData = repository.getMonthlyCalories()
                callback(monthlyData)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    fun refreshData() {
        loadTodayData()
    }
}