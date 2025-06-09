package com.example.alva.data.models

import java.util.Date

data class ChatMessage(
    val id: Long,
    val content: String,
    val type: MessageType,
    val timestamp: Date
)

enum class MessageType {
    USER, AI
}