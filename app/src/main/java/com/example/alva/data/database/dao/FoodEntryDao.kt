package com.example.alva.data.database.dao

import androidx.room.*
import com.example.alva.data.database.entities.FoodEntryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllFoodEntries(): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun getFoodEntryById(id: Long): FoodEntryEntity?

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startDate AND timestamp < :endDate ORDER BY timestamp DESC")
    suspend fun getFoodEntriesByDateRange(startDate: Date, endDate: Date): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    suspend fun getFoodEntriesSince(startDate: Date): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startDate AND timestamp < :endDate ORDER BY timestamp DESC")
    fun getFoodEntriesByDateRangeFlow(startDate: Date, endDate: Date): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY timestamp DESC")
    suspend fun getFoodEntriesForDay(date: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getFoodEntriesForDayFlow(date: Long): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodEntry: FoodEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntries(foodEntries: List<FoodEntryEntity>)

    @Update
    suspend fun updateFoodEntry(foodEntry: FoodEntryEntity)

    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntryById(id: Long)

    @Query("DELETE FROM food_entries")
    suspend fun deleteAllFoodEntries()

    @Query("SELECT SUM(calories) FROM food_entries WHERE timestamp >= :startDate AND timestamp < :endDate")
    suspend fun getTotalCaloriesForDateRange(startDate: Date, endDate: Date): Int?
}
