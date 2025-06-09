package com.example.alva.data.database.dao

import androidx.room.*
import com.example.alva.data.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserProfile(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getCurrentUserProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getCurrentUserProfileFlow(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllProfiles()
}