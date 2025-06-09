package com.example.alva.ui.aiassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alva.databinding.FragmentAiAssistantBinding
import com.example.alva.data.models.ChatMessage
import com.example.alva.data.models.MessageType

class AIAssistantFragment : Fragment() {

    private var _binding: FragmentAiAssistantBinding? = null
    private val binding get() = _binding!!

    private lateinit var aiAssistantViewModel: AIAssistantViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        aiAssistantViewModel = ViewModelProvider(this)[AIAssistantViewModel::class.java]
        _binding = FragmentAiAssistantBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding.root
    }

    private fun setupUI() {
        // Setup chat RecyclerView
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Show latest messages at bottom
            }
        }

        // Setup send button
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text?.toString()?.trim() ?: ""
            if (message.isNotBlank()) {
                aiAssistantViewModel.sendMessage(message)
                binding.editTextMessage.text?.clear()
            }
        }

        // Setup clear chat button (now in input area)
        binding.buttonClearChat.setOnClickListener {
            showClearChatConfirmationDialog()
        }

        // Setup quick suggestion buttons (keep suggestions always visible)
        binding.buttonSuggestDiet.setOnClickListener {
            aiAssistantViewModel.sendMessage("Can you suggest a diet plan for me?")
            // Don't hide suggestions - keep them visible
        }

        binding.buttonAnalyzeProgress.setOnClickListener {
            aiAssistantViewModel.sendMessage("Analyze my calorie progress for this week")
            // Don't hide suggestions - keep them visible
        }

        binding.buttonHealthyRecipes.setOnClickListener {
            aiAssistantViewModel.sendMessage("Give me some healthy recipe ideas")
            // Don't hide suggestions - keep them visible
        }

        binding.buttonNutritionTips.setOnClickListener {
            aiAssistantViewModel.sendMessage("Share some nutrition tips")
            // Don't hide suggestions - keep them visible
        }

        // Handle send action from keyboard
        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val message = binding.editTextMessage.text?.toString()?.trim() ?: ""
                if (message.isNotBlank()) {
                    aiAssistantViewModel.sendMessage(message)
                    binding.editTextMessage.text?.clear()
                }
                true
            } else {
                false
            }
        }
    }

    private fun showClearChatConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Chat History")
            .setMessage("Are you sure you want to clear all chat messages? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                aiAssistantViewModel.clearChat()
                Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                // Suggestions stay visible after clearing
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        aiAssistantViewModel.chatMessages.observe(viewLifecycleOwner) { messages: List<ChatMessage>? ->
            messages?.let { chatMessages ->
                chatAdapter.updateMessages(chatMessages)
                if (chatMessages.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(chatMessages.size - 1)
                }
                // Keep suggestions visible regardless of message count
                binding.layoutQuickSuggestions.visibility = View.VISIBLE
            }
        }

        aiAssistantViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSend.isEnabled = !isLoading

            // Disable clear button while loading
            binding.buttonClearChat.isEnabled = !isLoading
            binding.buttonClearChat.alpha = if (isLoading) 0.5f else 1.0f
        }

        aiAssistantViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                aiAssistantViewModel.clearError()
            }
        }

        aiAssistantViewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            // Keep suggestions always visible
            binding.layoutQuickSuggestions.visibility = View.VISIBLE
        }

        try {
            aiAssistantViewModel.networkStatus.observe(viewLifecycleOwner) { isOnline ->
                // Network status handling if needed
            }
        } catch (e: Exception) {
            // NetworkStatus observer doesn't exist, that's okay
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load initial conversation or show welcome message
        if (aiAssistantViewModel.chatMessages.value.isNullOrEmpty()) {
            aiAssistantViewModel.showWelcomeMessage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}