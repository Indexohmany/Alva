package com.example.alva.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

object ProfilePictureDebugHelper {
    private const val TAG = "ProfilePictureDebug"

    fun debugFileProviderSetup(context: Context) {
        Log.d(TAG, "=== Debugging FileProvider Setup ===")
        Log.d(TAG, "Package name: ${context.packageName}")
        Log.d(TAG, "Expected authority: ${context.packageName}.fileprovider")

        // Check if images directory exists
        val imagesDir = File(context.filesDir, "images")
        Log.d(TAG, "Images directory exists: ${imagesDir.exists()}")
        Log.d(TAG, "Images directory path: ${imagesDir.absolutePath}")

        if (imagesDir.exists()) {
            val files = imagesDir.listFiles()
            Log.d(TAG, "Files in images directory: ${files?.size ?: 0}")
        }

        // Test FileProvider
        try {
            val testFile = File(imagesDir, "test.txt")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            testFile.writeText("test")

            val testUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                testFile
            )
            Log.d(TAG, "FileProvider test successful: $testUri")
            testFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "FileProvider test failed", e)
        }
    }
}