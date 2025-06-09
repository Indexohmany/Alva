package com.example.alva.data.network

import com.example.alva.data.models.api.ApiResult
import com.example.alva.data.models.api.NetworkError
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    ApiResult.Success(body)
                } ?: ApiResult.Error(
                    Exception("Empty response body"),
                    "Response body is null"
                )
            } else {
                val errorMessage = parseErrorMessage(response)
                ApiResult.Error(
                    Exception("API Error: ${response.code()}"),
                    errorMessage
                )
            }
        } catch (exception: Exception) {
            val networkError = mapExceptionToNetworkError(exception)
            ApiResult.Error(networkError, getErrorMessage(networkError))
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val gson = Gson()

                // Try to parse Gemini error format first
                if (response.raw().request.url.host.contains("googleapis.com")) {
                    try {
                        val geminiErrorResponse = gson.fromJson(errorBody, GeminiErrorResponse::class.java)
                        geminiErrorResponse.error?.message ?: "Unknown Gemini API error"
                    } catch (e: Exception) {
                        // Fallback to generic error parsing
                        parseGenericError(errorBody, response)
                    }
                } else {
                    // Try to parse OpenAI error format
                    try {
                        val openaiErrorResponse = gson.fromJson(errorBody, OpenAIErrorResponse::class.java)
                        openaiErrorResponse.error?.message ?: "Unknown OpenAI API error"
                    } catch (e: Exception) {
                        // Fallback to generic error parsing
                        parseGenericError(errorBody, response)
                    }
                }
            } else {
                "HTTP ${response.code()}: ${response.message()}"
            }
        } catch (e: Exception) {
            "HTTP ${response.code()}: ${response.message()}"
        }
    }

    private fun <T> parseGenericError(errorBody: String, response: Response<T>): String {
        return when (response.code()) {
            400 -> "Bad Request: Please check your input"
            401 -> "Unauthorized: Invalid API key"
            403 -> "Forbidden: Access denied"
            404 -> "Not Found: API endpoint not found"
            429 -> "Too Many Requests: Rate limit exceeded"
            500 -> "Internal Server Error: Please try again later"
            502 -> "Bad Gateway: Server is temporarily unavailable"
            503 -> "Service Unavailable: Please try again later"
            else -> "HTTP ${response.code()}: ${response.message()}"
        }
    }

    private fun mapExceptionToNetworkError(exception: Exception): NetworkError {
        return when (exception) {
            is UnknownHostException,
            is NetworkConnectionException -> NetworkError.NoInternetConnection

            is SocketTimeoutException -> NetworkError.RequestTimeout

            is IOException -> NetworkError.ServerError

            else -> NetworkError.UnknownError(exception)
        }
    }

    private fun getErrorMessage(error: NetworkError): String {
        return when (error) {
            is NetworkError.NoInternetConnection ->
                "No internet connection. Please check your network settings."

            is NetworkError.RequestTimeout ->
                "Request timed out. Please try again."

            is NetworkError.ServerError ->
                "Server error. Please try again later."

            is NetworkError.UnauthorizedError ->
                "Authentication failed. Please check your API credentials."

            is NetworkError.RateLimitExceeded ->
                "Rate limit exceeded. Please wait before making another request."

            is NetworkError.ApiError ->
                "API Error (${error.code}): ${error.errorMessage}"

            is NetworkError.UnknownError ->
                "An unexpected error occurred: ${error.throwable.message}"
        }
    }

    // Helper function to retry API calls
    suspend fun <T> retryApiCall(
        maxRetries: Int = 3,
        delayMillis: Long = 1000,
        apiCall: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        for (attempt in 0 until maxRetries) {
            when (val result = apiCall()) {
                is ApiResult.Success -> return result
                is ApiResult.Error -> {
                    if (attempt == maxRetries - 1) return result
                    if (result.exception is NetworkError.NoInternetConnection ||
                        result.exception is NetworkError.UnauthorizedError) {
                        return result // Don't retry these errors
                    }
                    kotlinx.coroutines.delay(delayMillis * (attempt + 1))
                }
                is ApiResult.Loading -> {
                    // Continue to next iteration
                }
            }
        }
        return ApiResult.Error(
            NetworkError.UnknownError(Exception("Max retries exceeded")),
            "Request failed after $maxRetries attempts"
        )
    }
}

// Data classes for parsing error responses

// OpenAI Error Format
private data class OpenAIErrorResponse(
    val error: OpenAIErrorDetail?
)

private data class OpenAIErrorDetail(
    val message: String?,
    val type: String?,
    val code: String?
)

// Gemini Error Format
private data class GeminiErrorResponse(
    val error: GeminiErrorDetail?
)

private data class GeminiErrorDetail(
    val code: Int?,
    val message: String?,
    val status: String?
)