package com.example.alva.data.database.dao

import androidx.room.*
import com.example.alva.data.database.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettingsEntity?

    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun getSettingFlow(key: String): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettings(): List<AppSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<AppSettingsEntity>)

    @Update
    suspend fun updateSetting(setting: AppSettingsEntity)

    @Delete
    suspend fun deleteSetting(setting: AppSettingsEntity)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSettingByKey(key: String)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAllSettings()
}