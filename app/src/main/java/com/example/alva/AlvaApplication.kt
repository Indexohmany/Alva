package com.example.alva

import android.app.Application
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.network.NetworkManager

class AlvaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize core components
        DatabaseModule.getInstance(this)
        NetworkManager.getInstance(this)
    }
}