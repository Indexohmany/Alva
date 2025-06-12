package com.example.alva

import android.app.Application
import android.util.Log
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.network.NetworkManager
import com.example.alva.utils.ProfilePictureDebugHelper
import java.io.File

class AlvaApplication : Application() {

    companion object {
        private const val TAG = "AlvaApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Alva Application starting...")

        // Initialize core components (your existing code)
        DatabaseModule.getInstance(this)
        NetworkManager.getInstance(this)


        if (BuildConfig.DEBUG) {
            ProfilePictureDebugHelper.debugFileProviderSetup(this)
        }
    }

}