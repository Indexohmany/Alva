package com.example.alva.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.alva.data.database.converters.DateConverter
import java.util.Date

@Entity(tableName = "chat_messages")
@TypeConverters(DateConverter::class)
data class ChatMessageEntity(
    @PrimaryKey
    val id: Long,
    val content: String,
    val type: String, // USER, AI
    val timestamp: Date,
    val conversationId: String = "default" // For future conversation grouping
)