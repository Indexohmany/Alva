package com.example.alva.data.repository

import android.content.Context
import com.example.alva.data.database.DatabaseModule
import com.example.alva.data.database.mappers.toEntity
import com.example.alva.data.database.mappers.toModel
import com.example.alva.data.models.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(context: Context) {

    private val databaseModule = DatabaseModule.getInstance(context)
    private val chatMessageDao = databaseModule.chatMessageDao

    fun getChatMessages(conversationId: String = "default"): Flow<List<ChatMessage>> {
        return chatMessageDao.getChatMessages(conversationId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getChatMessagesList(conversationId: String = "default"): List<ChatMessage> {
        return chatMessageDao.getChatMessagesList(conversationId).map { it.toModel() }
    }

    suspend fun insertChatMessage(chatMessage: ChatMessage) {
        chatMessageDao.insertChatMessage(chatMessage.toEntity())
    }

    suspend fun deleteChatMessage(chatMessage: ChatMessage) {
        chatMessageDao.deleteChatMessage(chatMessage.toEntity())
    }

    suspend fun clearChatHistory(conversationId: String = "default") {
        chatMessageDao.deleteChatMessagesForConversation(conversationId)
    }

    suspend fun getChatMessageCount(conversationId: String = "default"): Int {
        return chatMessageDao.getChatMessageCount(conversationId)
    }
}