package com.example.lightmind.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

interface AiApiService {
    @POST("v1/chat/completions")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
}