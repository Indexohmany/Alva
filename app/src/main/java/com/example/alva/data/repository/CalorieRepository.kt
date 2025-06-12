package com.example.alva.data.repository

import android.content.Context
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.database.mappers.toEntity
import com.example.alva.data.database.mappers.toModel
import com.example.alva.data.models.FoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.roundToInt

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

    /**
     * Get calorie data for the last 7 days (including today)
     * Returns list of 7 integers representing daily calorie totals
     */
    suspend fun getWeeklyCalories(): List<Int> = withContext(Dispatchers.IO) {
        try {
            val weekCalories = mutableListOf<Int>()
            val calendar = Calendar.getInstance()

            // Get last 7 days including today
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

                val dayCalories = try {
                    foodEntryDao.getTotalCaloriesForDateRange(startOfDay, endOfDay) ?: 0
                } catch (e: Exception) {
                    0
                }
                weekCalories.add(dayCalories)
            }

            return@withContext weekCalories
        } catch (e: Exception) {
            // Return list of zeros if there's an error
            return@withContext List(7) { 0 }
        }
    }

    /**
     * Get calorie data for the last 30 days
     * Returns list of integers representing daily calorie totals
     */
    suspend fun getMonthlyCalories(): List<Int> = withContext(Dispatchers.IO) {
        try {
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

                val dayCalories = try {
                    foodEntryDao.getTotalCaloriesForDateRange(startOfDay, endOfDay) ?: 0
                } catch (e: Exception) {
                    0
                }
                monthCalories.add(dayCalories)
            }

            return@withContext monthCalories
        } catch (e: Exception) {
            // Return list of zeros if there's an error
            return@withContext List(30) { 0 }
        }
    }

    /**
     * Get today's total calories
     */
    suspend fun getTodayCalories(): Int = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()

            // Start of today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.time

            // End of today
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val todayEnd = calendar.time

            return@withContext foodEntryDao.getTotalCaloriesForDateRange(todayStart, todayEnd) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get average calories for a date range
     */
    suspend fun getAverageCalories(startDate: Date, endDate: Date): Double = withContext(Dispatchers.IO) {
        try {
            val entries = foodEntryDao.getFoodEntriesByDateRange(startDate, endDate)
            if (entries.isEmpty()) return@withContext 0.0

            // Calculate days between dates
            val daysDiff = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
            val totalCalories = entries.sumOf { it.calories }

            return@withContext totalCalories.toDouble() / daysDiff
        } catch (e: Exception) {
            0.0
        }
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