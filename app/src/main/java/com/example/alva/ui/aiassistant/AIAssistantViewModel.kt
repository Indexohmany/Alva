package com.example.alva.ui.aiassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.alva.data.models.ChatMessage
import com.example.alva.data.models.MessageType
import com.example.alva.data.models.api.ApiResult
import com.example.alva.data.repository.AIRepository
import com.example.alva.data.repository.CalorieRepository
import com.example.alva.data.repository.ChatRepository
import com.example.alva.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.*

class AIAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = AIRepository(application.applicationContext)
    private val calorieRepository = CalorieRepository(application.applicationContext)
    private val userRepository = UserRepository(application.applicationContext)
    private val chatRepository = ChatRepository(application.applicationContext)

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> = _suggestions

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatRepository.getChatMessages().collect { messages ->
                _chatMessages.value = messages
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            try {
                // Add user message to chat
                val userChatMessage = ChatMessage(
                    id = System.currentTimeMillis(),
                    content = userMessage,
                    type = MessageType.USER,
                    timestamp = Date()
                )

                // Save to database
                chatRepository.insertChatMessage(userChatMessage)

                _isLoading.value = true
                _errorMessage.value = null

                // Get user context for personalized responses
                val userProfile = userRepository.getUserProfile()
                val recentFoodEntries = calorieRepository.getRecentFoodEntries(7) // Last 7 days
                val calorieGoal = userProfile?.calorieGoal ?: 2000

                // Generate AI response based on user context
                val aiResult = when {
                    userMessage.contains("diet", ignoreCase = true) ||
                            userMessage.contains("suggest", ignoreCase = true) ||
                            userMessage.contains("meal", ignoreCase = true) -> {
                        aiRepository.generateDietSuggestion(userProfile, recentFoodEntries)
                    }

                    userMessage.contains("progress", ignoreCase = true) ||
                            userMessage.contains("analyze", ignoreCase = true) ||
                            userMessage.contains("track", ignoreCase = true) -> {
                        aiRepository.analyzeProgress(recentFoodEntries, calorieGoal)
                    }

                    userMessage.contains("recipe", ignoreCase = true) ||
                            userMessage.contains("cook", ignoreCase = true) -> {
                        aiRepository.suggestHealthyRecipes(userProfile?.dietaryPreferences)
                    }

                    userMessage.contains("tip", ignoreCase = true) ||
                            userMessage.contains("advice", ignoreCase = true) ||
                            userMessage.contains("help", ignoreCase = true) -> {
                        aiRepository.getNutritionTips(userProfile)
                    }

                    else -> {
                        aiRepository.generateGeneralResponse(userMessage, userProfile, recentFoodEntries)
                    }
                }

                // Handle the API result
                when (aiResult) {
                    is ApiResult.Success -> {
                        // Add AI response to chat
                        val aiChatMessage = ChatMessage(
                            id = System.currentTimeMillis() + 1,
                            content = aiResult.data,
                            type = MessageType.AI,
                            timestamp = Date()
                        )

                        // Save to database
                        chatRepository.insertChatMessage(aiChatMessage)

                        // Generate follow-up suggestions
                        generateSuggestions(userMessage, aiResult.data)
                        _networkStatus.value = true
                    }

                    is ApiResult.Error -> {
                        _errorMessage.value = aiResult.message

                        // Add fallback message
                        val fallbackContent = "⚠️ I'm having trouble connecting to my AI brain right now. Here's some general advice:\n\n" +
                                aiRepository.getFallbackResponse(userMessage)

                        val fallbackMessage = ChatMessage(
                            id = System.currentTimeMillis() + 1,
                            content = fallbackContent,
                            type = MessageType.AI,
                            timestamp = Date()
                        )

                        // Save fallback to database
                        chatRepository.insertChatMessage(fallbackMessage)
                        _networkStatus.value = false
                    }

                    is ApiResult.Loading -> {
                        // This case is handled by _isLoading
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred. Please try again."
                // Add error message to chat
                val errorMessage = ChatMessage(
                    id = System.currentTimeMillis() + 1,
                    content = "I apologize, but I'm experiencing technical difficulties. Please try again in a moment.",
                    type = MessageType.AI,
                    timestamp = Date()
                )
                chatRepository.insertChatMessage(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateSuggestions(userMessage: String, aiResponse: String) {
        val suggestions = mutableListOf<String>()

        when {
            userMessage.contains("diet", ignoreCase = true) -> {
                suggestions.addAll(listOf(
                    "How many calories should I eat daily?",
                    "What foods should I avoid?",
                    "Can you create a meal plan for me?"
                ))
            }

            userMessage.contains("recipe", ignoreCase = true) -> {
                suggestions.addAll(listOf(
                    "Show me low-calorie breakfast ideas",
                    "I need protein-rich meal suggestions",
                    "What are some healthy snacks?"
                ))
            }

            userMessage.contains("progress", ignoreCase = true) -> {
                suggestions.addAll(listOf(
                    "How can I improve my eating habits?",
                    "What's my average daily intake this month?",
                    "Give me tips to stay consistent"
                ))
            }

            else -> {
                suggestions.addAll(listOf(
                    "Suggest a diet plan for me",
                    "Analyze my calorie progress",
                    "Give me healthy recipe ideas",
                    "Share nutrition tips"
                ))
            }
        }

        _suggestions.value = suggestions.take(3) // Show max 3 suggestions
    }

    fun showWelcomeMessage() {
        viewModelScope.launch {
            // Check if we already have messages
            val existingMessages = chatRepository.getChatMessagesList()
            if (existingMessages.isEmpty()) {
                val welcomeMessage = ChatMessage(
                    id = System.currentTimeMillis(),
                    content = "Hello! I'm your AI nutrition assistant. I can help you with:\n\n" +
                            "• 🥗 Personalized diet suggestions\n" +
                            "• 📊 Calorie progress analysis\n" +
                            "• 🍳 Healthy recipe recommendations\n" +
                            "• 💡 Nutrition tips and advice\n\n" +
                            "What would you like to know?",
                    type = MessageType.AI,
                    timestamp = Date()
                )

                chatRepository.insertChatMessage(welcomeMessage)

                // Set initial suggestions
                _suggestions.value = listOf(
                    "Suggest a diet plan for me",
                    "Analyze my weekly progress",
                    "Give me healthy recipe ideas",
                    "Share nutrition tips"
                )
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
            showWelcomeMessage()
        }
    }

    fun regenerateLastResponse() {
        viewModelScope.launch {
            val messages = chatRepository.getChatMessagesList()
            if (messages.size >= 2) {
                // Remove last AI response and regenerate
                val userMessage = messages[messages.size - 2]
                if (userMessage.type == MessageType.USER) {
                    // Delete last AI message
                    val lastMessage = messages.last()
                    chatRepository.deleteChatMessage(lastMessage)

                    // Resend user message
                    sendMessage(userMessage.content)
                }
            }
        }
    }

    fun retryLastMessage() {
        viewModelScope.launch {
            val messages = chatRepository.getChatMessagesList()
            val lastUserMessage = messages.lastOrNull { it.type == MessageType.USER }
            lastUserMessage?.let {
                sendMessage(it.content)
            }
        }
    }
}