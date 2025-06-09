package com.example.alva.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.alva.BuildConfig
import com.example.alva.data.api.OpenAIApiService
import com.example.alva.data.api.GeminiApiService
import com.example.alva.data.api.OpenFoodFactsApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: NetworkManager? = null

        fun getInstance(context: Context): NetworkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Lazy initialization of services
    val openAIApiService: OpenAIApiService by lazy {
        createRetrofit(BuildConfig.OPENAI_API_URL).create(OpenAIApiService::class.java)
    }

    val geminiApiService: GeminiApiService by lazy {
        createRetrofit(BuildConfig.GEMINI_API_URL).create(GeminiApiService::class.java)
    }

    val openFoodFactsApiService: OpenFoodFactsApiService by lazy {
        createRetrofit("https://world.openfoodfacts.org/").create(OpenFoodFactsApiService::class.java)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createNetworkConnectionInterceptor())
            .addInterceptor(createAuthInterceptor())
            .build()
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private fun createNetworkConnectionInterceptor(): Interceptor {
        return Interceptor { chain ->
            if (!isNetworkAvailable()) {
                throw NetworkConnectionException("No internet connection available")
            }
            chain.proceed(chain.request())
        }
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            // Add authorization for OpenAI requests
            if (original.url.host.contains("openai.com")) {
                requestBuilder.addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
            }

            // Add User-Agent for OpenFoodFacts (recommended by their API guidelines)
            if (original.url.host.contains("openfoodfacts.org")) {
                requestBuilder.addHeader("User-Agent", "Alva - Diet Tracking App - Android")
            }

            // Note: Gemini API uses API key as query parameter, not header
            // This is handled in the GeminiApiService interface

            chain.proceed(requestBuilder.build())
        }
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}