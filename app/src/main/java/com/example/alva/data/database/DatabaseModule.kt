package com.example.alva.data.database

import android.content.Context

class DatabaseModule private constructor(context: Context) {

    val database: AlvaDatabase = AlvaDatabase.getDatabase(context)

    val userProfileDao = database.userProfileDao()
    val foodEntryDao = database.foodEntryDao()
    val chatMessageDao = database.chatMessageDao()
    val productInfoDao = database.productInfoDao()
    val appSettingsDao = database.appSettingsDao()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseModule? = null

        fun getInstance(context: Context): DatabaseModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseModule(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}