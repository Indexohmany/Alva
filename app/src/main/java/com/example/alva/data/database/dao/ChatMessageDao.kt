package com.example.alva.data.database.dao

import androidx.room.*
import com.example.alva.data.database.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getChatMessages(conversationId: String = "default"): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getChatMessagesList(conversationId: String = "default"): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :id")
    suspend fun getChatMessageById(id: Long): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessage: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(chatMessages: List<ChatMessageEntity>)

    @Update
    suspend fun updateChatMessage(chatMessage: ChatMessageEntity)

    @Delete
    suspend fun deleteChatMessage(chatMessage: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteChatMessagesForConversation(conversationId: String = "default")

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllChatMessages()

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun getChatMessageCount(conversationId: String = "default"): Int
}