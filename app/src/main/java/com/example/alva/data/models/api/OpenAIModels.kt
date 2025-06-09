package com.example.alva.data.models.api

import com.google.gson.annotations.SerializedName

// Request Models
data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatCompletionMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 500,
    val temperature: Double = 0.7,
    @SerializedName("top_p")
    val topP: Double = 1.0,
    val stream: Boolean = false
)

data class ChatCompletionMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

// Response Models
data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: TokenUsage?,
    val error: OpenAIError?
)

data class ChatChoice(
    val index: Int,
    val message: ChatCompletionMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class TokenUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

data class OpenAIError(
    val message: String,
    val type: String,
    val param: String?,
    val code: String?
)

// Generic API Response Wrapper
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val exception: Exception, val message: String) : ApiResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : ApiResult<T>()
}

// Network Error Types
sealed class NetworkError : Exception() {
    object NoInternetConnection : NetworkError()
    object RequestTimeout : NetworkError()
    object ServerError : NetworkError()
    object UnauthorizedError : NetworkError()
    object RateLimitExceeded : NetworkError()
    data class ApiError(val code: Int, val errorMessage: String) : NetworkError()
    data class UnknownError(val throwable: Throwable) : NetworkError()
}


// Gemini Request Models
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val safetySettings: List<GeminiSafetySetting>? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiGenerationConfig(
    val temperature: Double? = 0.7,
    val topK: Int? = null,
    val topP: Double? = null,
    val maxOutputTokens: Int? = 500,
    val stopSequences: List<String>? = null
)

data class GeminiSafetySetting(
    val category: String,
    val threshold: String
)

// Gemini Response Models
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val promptFeedback: GeminiPromptFeedback?,
    val error: GeminiError?
)

data class GeminiCandidate(
    val content: GeminiContent?,
    val finishReason: String?,
    val index: Int?,
    val safetyRatings: List<GeminiSafetyRating>?
)

data class GeminiPromptFeedback(
    val safetyRatings: List<GeminiSafetyRating>?,
    val blockReason: String?
)

data class GeminiSafetyRating(
    val category: String,
    val probability: String
)

data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

// Vision support
// GeminiPart to support both text and images
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiImageData? = null
)

// Image data for Gemini Vision API
data class GeminiImageData(
    val mimeType: String,
    val data: String
)

// OpenFoodFacts API Models
data class OpenFoodFactsResponse(
    val status: Int,
    val code: String?,
    val product: OpenFoodFactsProduct?
)

data class OpenFoodFactsProduct(
    @SerializedName("product_name")
    val productName: String?,

    @SerializedName("brands")
    val brands: String?,

    @SerializedName("quantity")
    val quantity: String?,

    @SerializedName("serving_size")
    val servingSize: String?,

    @SerializedName("nutriments")
    val nutriments: OpenFoodFactsNutriments?
)

data class OpenFoodFactsNutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Float?,

    @SerializedName("energy-kcal_serving")
    val energyKcalServing: Float?,

    @SerializedName("proteins_100g")
    val proteins100g: Float?,

    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Float?,

    @SerializedName("fat_100g")
    val fat100g: Float?,

    @SerializedName("fiber_100g")
    val fiber100g: Float?,

    @SerializedName("sugars_100g")
    val sugars100g: Float?,

    @SerializedName("sodium_100g")
    val sodium100g: Float?
)

// Response parsing for Gemini food recognition
data class GeminiFoodResponse(
    val name: String,
    val estimatedCalories: Int,
    val confidence: Float,
    val alternatives: List<String>?,
    val nutritionInfo: GeminiNutritionInfo?
)

data class GeminiNutritionInfo(
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float
)

