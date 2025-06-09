package com.example.alva.data.repository

import android.content.Context
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.database.mappers.toEntity
import com.example.alva.data.database.mappers.toModel
import com.example.alva.data.models.FoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class CalorieRepository(context: Context) {

    private val databaseModule = DatabaseModule.getInstance(context)
    private val foodEntryDao = databaseModule.foodEntryDao
    private val userRepository = UserRepository(context)

    suspend fun addFoodEntry(foodEntry: FoodEntry) {
        val entryWithId = if (foodEntry.id == 0L) {
            foodEntry.copy(id = System.currentTimeMillis())
        } else {
            foodEntry
        }
        foodEntryDao.insertFoodEntry(entryWithId.toEntity())
    }

    suspend fun deleteFoodEntry(entryId: Long) {
        foodEntryDao.deleteFoodEntryById(entryId)
    }

    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        foodEntryDao.updateFoodEntry(foodEntry.toEntity())
    }

    suspend fun getTodayFoodEntries(): List<FoodEntry> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return foodEntryDao.getFoodEntriesByDateRange(today, tomorrow).map { it.toModel() }
    }

    fun getTodayFoodEntriesFlow(): Flow<List<FoodEntry>> {
        val todayMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return foodEntryDao.getFoodEntriesForDayFlow(todayMillis).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getRecentFoodEntries(days: Int): List<FoodEntry> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -days)
        }.time

        return foodEntryDao.getFoodEntriesSince(cutoffDate).map { it.toModel() }
    }

    suspend fun getWeeklyCalories(): List<Int> {
        val weekCalories = mutableListOf<Int>()
        val calendar = Calendar.getInstance()

        // Get last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)

            val startOfDay = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfDay = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val dayCalories = foodEntryDao.getTotalCaloriesForDateRange(startOfDay, endOfDay) ?: 0
            weekCalories.add(dayCalories)
        }

        return weekCalories
    }

    suspend fun getMonthlyCalories(): List<Int> {
        val monthCalories = mutableListOf<Int>()
        val calendar = Calendar.getInstance()

        // Get last 30 days
        for (i in 29 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)

            val startOfDay = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfDay = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val dayCalories = foodEntryDao.getTotalCaloriesForDateRange(startOfDay, endOfDay) ?: 0
            monthCalories.add(dayCalories)
        }

        return monthCalories
    }

    suspend fun updateCalorieGoal(newGoal: Int) {
        val currentProfile = userRepository.getUserProfile()
        currentProfile?.let { profile ->
            userRepository.updateUserProfile(profile.copy(calorieGoal = newGoal))
        }
    }

    suspend fun getCalorieGoal(): Int {
        return userRepository.getUserProfile()?.calorieGoal ?: 2000
    }

    suspend fun getTotalCaloriesToday(): Int {
        val todayEntries = getTodayFoodEntries()
        return todayEntries.sumOf { it.calories }
    }

    suspend fun getFoodEntryById(id: Long): FoodEntry? {
        return foodEntryDao.getFoodEntryById(id)?.toModel()
    }

    fun getAllFoodEntriesFlow(): Flow<List<FoodEntry>> {
        return foodEntryDao.getAllFoodEntries().map { entities ->
            entities.map { it.toModel() }
        }
    }
}