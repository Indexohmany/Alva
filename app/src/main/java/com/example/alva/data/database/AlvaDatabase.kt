package com.example.alva.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.alva.data.database.converters.DateConverter
import com.example.alva.data.database.converters.StringListConverter
import com.example.alva.data.database.dao.*
import com.example.alva.data.database.entities.*

@Database(
    entities = [
        UserProfileEntity::class,
        FoodEntryEntity::class,
        ChatMessageEntity::class,
        ProductInfoEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class AlvaDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun productInfoDao(): ProductInfoDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AlvaDatabase? = null

        fun getDatabase(context: Context): AlvaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlvaDatabase::class.java,
                    "alva_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2) // For future migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Callback for database initialization
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default data if needed
                // This runs on a background thread
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Database has been opened
            }
        }

        // Example migration (for future use)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration logic when database schema changes
                // Example:
                // database.execSQL("ALTER TABLE user_profiles ADD COLUMN new_column TEXT")
            }
        }
    }
}