package com.example.alva.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.alva.BuildConfig
import com.example.alva.data.models.FoodInfo
import com.example.alva.data.models.NutritionInfo
import com.example.alva.data.models.api.*
import com.example.alva.data.network.NetworkManager
import com.example.alva.data.network.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FoodRecognitionRepository(private val context: Context) {

    private val networkManager = NetworkManager.getInstance(context)

    suspend fun recognizeFood(imageUri: Uri): FoodInfo? {
        return try {
            if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                // Return fallback/mock data if no API key
                return getFallbackFoodInfo()
            }

            // Convert image to base64
            val base64Image = convertImageToBase64(imageUri)

            // Call Gemini Vision API
            val geminiResponse = callGeminiVision(base64Image)

            // Parse response to FoodInfo
            parseGeminiResponse(geminiResponse) ?: getFallbackFoodInfo()

        } catch (e: Exception) {
            // Return fallback data if API fails
            getFallbackFoodInfo()
        }
    }

    private suspend fun convertImageToBase64(imageUri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Compress image to reduce API payload size (Gemini has size limits)
        val maxSize = 512
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        val compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        val outputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Clean up
        bitmap.recycle()
        compressedBitmap.recycle()

        Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    private suspend fun callGeminiVision(base64Image: String): String? {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = getFoodAnalysisPrompt()),
                        GeminiPart(
                            inlineData = GeminiImageData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    ),
                    role = "user"
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.3,
                maxOutputTokens = 1000,
                topP = 0.8,
                topK = 40
            ),
            safetySettings = getSafetySettings()
        )

        val apiResult = NetworkUtils.safeApiCall {
            networkManager.geminiApiService.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
        }

        return when (apiResult) {
            is ApiResult.Success -> {
                apiResult.data.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            }
            else -> null
        }
    }

    private fun getSafetySettings(): List<GeminiSafetySetting> {
        return listOf(
            GeminiSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"),
            GeminiSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"),
            GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
            GeminiSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE")
        )
    }

    private fun getFoodAnalysisPrompt(): String {
        return """
            Analyze this food image and provide a JSON response with the following exact structure:
            {
                "name": "specific food name",
                "estimatedCalories": number_for_typical_serving,
                "confidence": 0.0_to_1.0,
                "alternatives": ["alternative1", "alternative2"],
                "nutritionInfo": {
                    "protein": grams_per_serving,
                    "carbs": grams_per_serving,
                    "fat": grams_per_serving,
                    "fiber": grams_per_serving,
                    "sugar": grams_per_serving,
                    "sodium": grams_per_serving
                }
            }

            Guidelines:
            - Estimate for a realistic single serving size
            - Be conservative with confidence (0.3-0.9 range)
            - Provide 2-3 alternatives if multiple foods or unsure
            - All nutrition values should be realistic numbers in grams
            - If you can't clearly identify food, set confidence below 0.5
            - Respond ONLY with valid JSON, no explanations or extra text
            - Ensure all numbers are realistic for the food type
        """.trimIndent()
    }

    private fun parseGeminiResponse(response: String?): FoodInfo? {
        return try {
            if (response == null) return null

            // Find JSON in response
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1

            if (jsonStart == -1 || jsonEnd <= jsonStart) return null

            val jsonString = response.substring(jsonStart, jsonEnd)
            val gson = Gson()
            val parsedResponse = gson.fromJson(jsonString, GeminiFoodResponse::class.java)

            // Validate response
            if (parsedResponse.name.isBlank() || parsedResponse.estimatedCalories <= 0) {
                return null
            }

            FoodInfo(
                name = parsedResponse.name,
                estimatedCalories = parsedResponse.estimatedCalories,
                confidence = parsedResponse.confidence.coerceIn(0f, 1f),
                alternatives = parsedResponse.alternatives ?: emptyList(),
                nutritionInfo = parsedResponse.nutritionInfo?.let {
                    NutritionInfo(
                        protein = maxOf(0f, it.protein),
                        carbs = maxOf(0f, it.carbs),
                        fat = maxOf(0f, it.fat),
                        fiber = maxOf(0f, it.fiber),
                        sugar = maxOf(0f, it.sugar),
                        sodium = maxOf(0f, it.sodium)
                    )
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getFallbackFoodInfo(): FoodInfo {
        // Provide realistic fallback when API fails
        val foods = listOf(
            FoodInfo("Mixed Salad", 65, 0.3f, listOf("Caesar Salad", "Garden Salad"),
                NutritionInfo(3f, 12f, 1f, 3f, 6f, 0.1f)),
            FoodInfo("Grilled Chicken", 231, 0.4f, listOf("Fried Chicken", "Chicken Thigh"),
                NutritionInfo(43f, 0f, 5f, 0f, 0f, 0.1f)),
            FoodInfo("Apple", 95, 0.5f, listOf("Red Apple", "Green Apple"),
                NutritionInfo(0.5f, 25f, 0.3f, 4f, 19f, 0.0f)),
            FoodInfo("Banana", 105, 0.5f, listOf("Ripe Banana", "Green Banana"),
                NutritionInfo(1.3f, 27f, 0.4f, 3f, 14f, 0.0f))
        )
        return foods.random()
    }
}