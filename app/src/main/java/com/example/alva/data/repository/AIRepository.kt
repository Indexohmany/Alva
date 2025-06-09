package com.example.alva.data.repository

import android.content.Context
import com.example.alva.BuildConfig
import com.example.alva.data.models.FoodEntry
import com.example.alva.data.models.UserProfile
import com.example.alva.data.models.api.ApiResult
import com.example.alva.data.models.api.GeminiContent
import com.example.alva.data.models.api.GeminiGenerationConfig
import com.example.alva.data.models.api.GeminiPart
import com.example.alva.data.models.api.GeminiRequest
import com.example.alva.data.models.api.GeminiSafetySetting
import com.example.alva.data.network.NetworkManager
import com.example.alva.data.network.NetworkUtils

class AIRepository(context: Context) {

    private val networkManager = NetworkManager.getInstance(context)

    suspend fun generateDietSuggestion(userProfile: UserProfile?, recentEntries: List<FoodEntry>): ApiResult<String> {
        val prompt = buildDietSuggestionPrompt(userProfile, recentEntries)
        return makeAIRequest(prompt)
    }

    suspend fun analyzeProgress(recentEntries: List<FoodEntry>, calorieGoal: Int): ApiResult<String> {
        val prompt = buildProgressAnalysisPrompt(recentEntries, calorieGoal)
        return makeAIRequest(prompt)
    }

    suspend fun suggestHealthyRecipes(dietaryPreferences: List<String>?): ApiResult<String> {
        val prompt = buildRecipeSuggestionPrompt(dietaryPreferences)
        return makeAIRequest(prompt)
    }

    suspend fun getNutritionTips(userProfile: UserProfile?): ApiResult<String> {
        val prompt = buildNutritionTipsPrompt(userProfile)
        return makeAIRequest(prompt)
    }

    suspend fun generateGeneralResponse(message: String, userProfile: UserProfile?, recentEntries: List<FoodEntry>): ApiResult<String> {
        val prompt = buildGeneralResponsePrompt(message, userProfile, recentEntries)
        return makeAIRequest(prompt)
    }

    private suspend fun makeAIRequest(userPrompt: String): ApiResult<String> {
        // Check if API key is configured
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return ApiResult.Error(
                Exception("API key not configured"),
                "Gemini API key is not configured. Please add your API key to local.properties"
            )
        }

        return NetworkUtils.retryApiCall<String>(maxRetries = 2) {
            makeApiCallInternal(userPrompt)
        }
    }

    private suspend fun makeApiCallInternal(userPrompt: String): ApiResult<String> {
        val apiResult = NetworkUtils.safeApiCall {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = "${getSystemPrompt()}\n\nUser: $userPrompt")
                        ),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7,
                    maxOutputTokens = 500,
                    topP = 0.95,
                    topK = 40
                ),
                safetySettings = getSafetySettings()
            )

            networkManager.geminiApiService.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
        }

        return when (apiResult) {
            is ApiResult.Success -> {
                val response = apiResult.data.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (response != null) {
                    ApiResult.Success(response.trim())
                } else {
                    // Check for errors in response
                    val errorMessage = when {
                        apiResult.data.error != null -> "API Error: ${apiResult.data.error.message}"
                        apiResult.data.promptFeedback?.blockReason != null -> "Content blocked: ${apiResult.data.promptFeedback.blockReason}"
                        else -> "No response received from AI"
                    }
                    ApiResult.Error(
                        Exception("Empty AI response"),
                        errorMessage
                    )
                }
            }
            is ApiResult.Error -> {
                ApiResult.Error(apiResult.exception, apiResult.message)
            }
            is ApiResult.Loading -> {
                ApiResult.Loading()
            }
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

    private fun getSystemPrompt(): String {
        return """
            You are a helpful AI nutrition assistant for a diet tracking app called Alva. 
            Your role is to provide personalized, practical nutrition advice based on user data.
            
            Guidelines:
            - Be encouraging and supportive
            - Provide specific, actionable advice
            - Use emojis to make responses more engaging
            - Keep responses concise but informative (under 400 words, but you can go further if needed)
            - Include calorie estimates when suggesting foods
            - Always remind users to consult healthcare professionals for medical advice
            - Focus on balanced nutrition and healthy habits
            - Consider the user's goals, preferences, and current intake
            
            Format your responses with clear sections.
        """.trimIndent()
    }

    private fun buildDietSuggestionPrompt(userProfile: UserProfile?, recentEntries: List<FoodEntry>): String {
        val age = userProfile?.age ?: 25
        val weight = userProfile?.weight ?: 70f
        val goal = userProfile?.calorieGoal ?: 2000
        val preferences = userProfile?.dietaryPreferences?.joinToString(", ") ?: "None specified"
        val recentCalories = if (recentEntries.isNotEmpty()) {
            recentEntries.take(7).sumOf { it.calories } / recentEntries.take(7).size
        } else 0

        return """
            Create a personalized diet suggestion for a user with the following details:
            - Age: $age years
            - Weight: $weight kg
            - Daily calorie goal: $goal calories
            - Dietary preferences: $preferences
            - Recent daily average intake: $recentCalories calories
            
            Please provide a balanced meal plan for one day with breakfast, lunch, dinner, and a snack.
            Include calorie estimates for each meal and explain why this plan suits their current situation.
        """.trimIndent()
    }

    private fun buildProgressAnalysisPrompt(recentEntries: List<FoodEntry>, calorieGoal: Int): String {
        val weeklyCalories = recentEntries.take(7).sumOf { it.calories }
        val dailyAverage = if (recentEntries.isNotEmpty()) weeklyCalories / 7 else 0
        val commonFoods = recentEntries.groupBy { it.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        return """
            Analyze the following weekly nutrition data and provide insights:
            - Daily calorie goal: $calorieGoal calories
            - Daily average intake: $dailyAverage calories
            - Total days tracked: 7
            - Most consumed foods: ${commonFoods.joinToString(", ") { "${it.first} (${it.second} times)" }}
            
            Please provide:
            1. Progress assessment
            2. Key insights about eating patterns
            3. Specific recommendations for improvement
            4. Encouragement based on performance
        """.trimIndent()
    }

    private fun buildRecipeSuggestionPrompt(dietaryPreferences: List<String>?): String {
        val preferences = dietaryPreferences?.joinToString(", ") ?: "No specific preferences"

        return """
            Suggest 3 healthy, easy-to-make recipes considering these dietary preferences: $preferences
            
            For each recipe, include:
            - Recipe name and estimated calories per serving
            - Simple ingredient list
            - Brief cooking instructions
            - Nutritional benefits
            
            Focus on balanced macronutrients and practical preparation.
        """.trimIndent()
    }

    private fun buildNutritionTipsPrompt(userProfile: UserProfile?): String {
        val age = userProfile?.age ?: 25
        val goal = userProfile?.calorieGoal ?: 2000
        val preferences = userProfile?.dietaryPreferences?.joinToString(", ") ?: "None"

        return """
            Provide 5-7 personalized nutrition tips for someone with:
            - Age: $age years
            - Daily calorie goal: $goal calories
            - Dietary preferences: $preferences
            
            Include practical, actionable advice that can be easily implemented in daily life.
            Focus on sustainable habits rather than quick fixes.
        """.trimIndent()
    }

    private fun buildGeneralResponsePrompt(message: String, userProfile: UserProfile?, recentEntries: List<FoodEntry>): String {
        val context = if (userProfile != null) {
            "User context: ${userProfile.age} years old, ${userProfile.calorieGoal} calorie goal"
        } else {
            "No specific user context available"
        }

        return """
            User question: "$message"
            
            $context
            Recent food entries: ${recentEntries.take(3).joinToString(", ") { "${it.name} (${it.calories} cal)" }}
            
            Please provide a helpful, personalized response related to nutrition and diet.
            If the question is not nutrition-related, gently redirect to nutrition topics while being helpful.
        """.trimIndent()
    }

    // Fallback responses for when API is unavailable
    fun getFallbackResponse(message: String): String {
        return when {
            message.contains("diet", ignoreCase = true) -> {
                "🥗 **Diet Suggestions**\n\n" +
                        "Focus on balanced meals with lean proteins, whole grains, fruits, and vegetables. " +
                        "Try to eat regular meals and stay hydrated throughout the day.\n\n" +
                        "• **Breakfast:** Oatmeal with berries\n" +
                        "• **Lunch:** Grilled chicken salad\n" +
                        "• **Dinner:** Baked fish with vegetables\n" +
                        "• **Snack:** Greek yogurt with nuts"
            }
            message.contains("recipe", ignoreCase = true) -> {
                "🍳 **Healthy Recipe Ideas**\n\n" +
                        "Try these simple, nutritious meals:\n\n" +
                        "• **Grilled chicken with roasted vegetables** (400 cal)\n" +
                        "• **Quinoa salad with mixed vegetables** (350 cal)\n" +
                        "• **Greek yogurt with fruits and nuts** (200 cal)\n" +
                        "• **Baked salmon with sweet potato** (450 cal)"
            }
            message.contains("progress", ignoreCase = true) -> {
                "📊 **Progress Tips**\n\n" +
                        "Keep tracking your food intake consistently. " +
                        "Focus on meeting your daily calorie goals and eating a variety of nutritious foods.\n\n" +
                        "• Track everything you eat\n" +
                        "• Stay within your calorie goal\n" +
                        "• Include all food groups\n" +
                        "• Drink plenty of water"
            }
            else -> {
                "💡 **General Nutrition Advice**\n\n" +
                        "Remember to stay hydrated, eat regular balanced meals, and listen to your body's hunger cues. " +
                        "Consistency is key to reaching your nutrition goals!\n\n" +
                        "I can help you with:\n" +
                        "• Diet suggestions\n" +
                        "• Recipe ideas\n" +
                        "• Progress analysis\n" +
                        "• Nutrition tips"
            }
        }
    }
}