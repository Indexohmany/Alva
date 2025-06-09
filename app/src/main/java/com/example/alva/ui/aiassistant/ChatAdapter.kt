package com.example.alva.ui.aiassistant

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alva.R
import com.example.alva.databinding.ItemChatMessageBinding
import com.example.alva.data.models.ChatMessage
import com.example.alva.data.models.MessageType
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatMessageViewHolder>() {

    private var messages = listOf<ChatMessage>()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ChatMessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.apply {
                // Format message content with basic markdown support for AI messages
                val formattedText = when (message.type) {
                    MessageType.AI -> formatBasicMarkdown(message.content)
                    MessageType.USER -> SpannableString(message.content)
                }

                textMessage.text = formattedText
                textTime.text = timeFormat.format(message.timestamp)

                // Style and align message based on type
                when (message.type) {
                    MessageType.USER -> {
                        // User messages - right aligned, blue theme
                        try {
                            cardMessage.setCardBackgroundColor(
                                ContextCompat.getColor(root.context, R.color.chat_message_user_background)
                            )
                            textMessage.setTextColor(
                                ContextCompat.getColor(root.context, R.color.chat_message_user_text)
                            )
                        } catch (e: Exception) {
                            // Fallback colors
                            cardMessage.setCardBackgroundColor(
                                ContextCompat.getColor(root.context, R.color.primary_light)
                            )
                            textMessage.setTextColor(
                                ContextCompat.getColor(root.context, R.color.primary_text)
                            )
                        }

                        // Align card to the right
                        val cardParams = cardMessage.layoutParams as LinearLayout.LayoutParams
                        cardParams.gravity = Gravity.END
                        cardMessage.layoutParams = cardParams

                        // Align timestamp to the right
                        val timeParams = textTime.layoutParams as LinearLayout.LayoutParams
                        timeParams.gravity = Gravity.END
                        timeParams.setMargins(0, 0, 48, 0) // Right margin
                        textTime.layoutParams = timeParams
                    }
                    MessageType.AI -> {
                        // AI messages - left aligned, gray theme
                        try {
                            cardMessage.setCardBackgroundColor(
                                ContextCompat.getColor(root.context, R.color.chat_message_ai_background)
                            )
                            textMessage.setTextColor(
                                ContextCompat.getColor(root.context, R.color.chat_message_ai_text)
                            )
                        } catch (e: Exception) {
                            // Fallback colors
                            cardMessage.setCardBackgroundColor(
                                ContextCompat.getColor(root.context, R.color.surface_color)
                            )
                            textMessage.setTextColor(
                                ContextCompat.getColor(root.context, R.color.primary_text)
                            )
                        }

                        // Align card to the left
                        val cardParams = cardMessage.layoutParams as LinearLayout.LayoutParams
                        cardParams.gravity = Gravity.START
                        cardMessage.layoutParams = cardParams

                        // Align timestamp to the left
                        val timeParams = textTime.layoutParams as LinearLayout.LayoutParams
                        timeParams.gravity = Gravity.START
                        timeParams.setMargins(48, 0, 0, 0) // Left margin
                        textTime.layoutParams = timeParams
                    }
                }
            }
        }

        private fun formatBasicMarkdown(text: String): SpannableString {
            val spannable = SpannableString(text)

            // Simple bold formatting for **text**
            formatBold(spannable)

            // Simple bullet point highlighting
            formatBullets(spannable)

            return spannable
        }

        private fun formatBold(spannable: SpannableString) {
            val text = spannable.toString()
            var start = 0

            while (true) {
                val boldStart = text.indexOf("**", start)
                if (boldStart == -1) break

                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd == -1) break

                // Apply bold to the content between ** (keep the ** visible for now)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    boldStart,
                    boldEnd + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                start = boldEnd + 2
            }
        }

        private fun formatBullets(spannable: SpannableString) {
            val text = spannable.toString()
            val lines = text.split("\n")
            var position = 0

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("•") || trimmed.startsWith("-")) {
                    val bulletStart = text.indexOf(trimmed, position)
                    if (bulletStart >= 0 && bulletStart + 1 < spannable.length) {
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            bulletStart,
                            bulletStart + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                position += line.length + 1
            }
        }
    }
}