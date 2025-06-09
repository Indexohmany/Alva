package com.example.alva.ui.camera

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alva.data.models.FoodInfo
import com.example.alva.data.models.ProductInfo
import com.example.alva.data.models.FoodEntry
import com.example.alva.data.repository.CalorieRepository
import com.example.alva.data.repository.FoodRecognitionRepository
import com.example.alva.data.repository.ProductRepository
import kotlinx.coroutines.launch
import java.util.Date

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val calorieRepository = CalorieRepository(application.applicationContext)
    private val foodRecognitionRepository = FoodRecognitionRepository(application.applicationContext)
    private val productRepository = ProductRepository(application.applicationContext)

    private val _recognizedFood = MutableLiveData<FoodInfo?>()
    val recognizedFood: LiveData<FoodInfo?> = _recognizedFood

    private val _scannedProduct = MutableLiveData<ProductInfo?>()
    val scannedProduct: LiveData<ProductInfo?> = _scannedProduct

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun recognizeFood(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Use Gemini Vision API or fallback recognition
                val recognizedFood = foodRecognitionRepository.recognizeFood(imageUri)

                if (recognizedFood != null) {
                    _recognizedFood.value = recognizedFood
                } else {
                    _errorMessage.value = "Could not recognize food. Please try again or add manually."
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error recognizing food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun lookupProduct(barcode: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Look up product using OpenFoodFacts API
                val productInfo = productRepository.getProductByBarcode(barcode)

                if (productInfo != null) {
                    _scannedProduct.value = productInfo
                } else {
                    _errorMessage.value = "Product not found. Please add manually."
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error looking up product: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addRecognizedFood(foodName: String, calories: Int, quantity: Float = 1f, unit: String = "serving") {
        viewModelScope.launch {
            try {
                val recognizedFoodInfo = _recognizedFood.value
                val nutritionInfo = recognizedFoodInfo?.nutritionInfo

                val foodEntry = FoodEntry(
                    id = System.currentTimeMillis(),
                    name = foodName,
                    calories = calories,
                    timestamp = Date(),
                    source = "Camera Recognition",
                    protein = (nutritionInfo?.protein ?: 0f) * quantity,
                    carbs = (nutritionInfo?.carbs ?: 0f) * quantity,
                    fat = (nutritionInfo?.fat ?: 0f) * quantity,
                    quantity = quantity,
                    unit = unit
                )

                calorieRepository.addFoodEntry(foodEntry)
                _recognizedFood.value = null // Clear the result

            } catch (e: Exception) {
                _errorMessage.value = "Error adding food: ${e.message}"
            }
        }
    }

    fun addScannedProduct(productName: String, calories: Int, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                val scannedProductInfo = _scannedProduct.value
                val nutritionInfo = scannedProductInfo?.nutritionInfo

                val foodEntry = FoodEntry(
                    id = System.currentTimeMillis(),
                    name = productName,
                    calories = calories,
                    timestamp = Date(),
                    source = "Barcode Scan",
                    protein = (nutritionInfo?.protein ?: 0f) * quantity,
                    carbs = (nutritionInfo?.carbs ?: 0f) * quantity,
                    fat = (nutritionInfo?.fat ?: 0f) * quantity,
                    quantity = quantity,
                    unit = scannedProductInfo?.servingSize ?: "serving"
                )

                calorieRepository.addFoodEntry(foodEntry)
                _scannedProduct.value = null // Clear the result

            } catch (e: Exception) {
                _errorMessage.value = "Error adding product: ${e.message}"
            }
        }
    }

    // Enhanced method to add food with complete nutrition info
    fun addRecognizedFoodWithNutrition(
        foodInfo: FoodInfo,
        confirmedName: String,
        confirmedCalories: Int,
        quantity: Float = 1f,
        unit: String = "serving"
    ) {
        viewModelScope.launch {
            try {
                val nutritionInfo = foodInfo.nutritionInfo

                val foodEntry = FoodEntry(
                    id = System.currentTimeMillis(),
                    name = confirmedName,
                    calories = confirmedCalories,
                    timestamp = Date(),
                    source = "Camera Recognition (AI Confidence: ${String.format("%.1f", foodInfo.confidence * 100)}%)",
                    protein = (nutritionInfo?.protein ?: 0f) * quantity,
                    carbs = (nutritionInfo?.carbs ?: 0f) * quantity,
                    fat = (nutritionInfo?.fat ?: 0f) * quantity,
                    quantity = quantity,
                    unit = unit
                )

                calorieRepository.addFoodEntry(foodEntry)
                _recognizedFood.value = null

            } catch (e: Exception) {
                _errorMessage.value = "Error adding food with nutrition: ${e.message}"
            }
        }
    }

    // Enhanced method to add product with complete nutrition info
    fun addScannedProductWithNutrition(
        productInfo: ProductInfo,
        confirmedCalories: Int,
        quantity: Float = 1f
    ) {
        viewModelScope.launch {
            try {
                val nutritionInfo = productInfo.nutritionInfo

                val foodEntry = FoodEntry(
                    id = System.currentTimeMillis(),
                    name = productInfo.name,
                    calories = confirmedCalories,
                    timestamp = Date(),
                    source = "Barcode Scan - ${productInfo.brand}",
                    protein = (nutritionInfo?.protein ?: 0f) * quantity,
                    carbs = (nutritionInfo?.carbs ?: 0f) * quantity,
                    fat = (nutritionInfo?.fat ?: 0f) * quantity,
                    quantity = quantity,
                    unit = productInfo.servingSize
                )

                calorieRepository.addFoodEntry(foodEntry)
                _scannedProduct.value = null

            } catch (e: Exception) {
                _errorMessage.value = "Error adding product with nutrition: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearResults() {
        _recognizedFood.value = null
        _scannedProduct.value = null
    }
}