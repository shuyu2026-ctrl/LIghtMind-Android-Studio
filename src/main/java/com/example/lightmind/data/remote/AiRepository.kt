package com.example.lightmind.data.remote

import android.content.Context
import android.util.Log
import com.example.lightmind.BuildConfig
import com.example.lightmind.R
import com.example.lightmind.data.local.NoteDao
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AiRepository(
    private val context: Context,
    private val noteDao: NoteDao
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deepseek.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(createOkHttpClient())
        .build()

    private val apiService = retrofit.create(AiApiService::class.java)

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun askQuestion(question: String): String {
        return try {
            val recentNotes = noteDao.getRecentNotes(20)
            Log.d("AiRepository", "获取到 ${recentNotes.size} 条笔记")

            val notesContext = if (recentNotes.isNotEmpty()) {
                buildString {
                    append(context.getString(R.string.prompt_notes_header))
                    append("\n\n")
                    recentNotes.forEachIndexed { index, note ->
                        val content = if (note.content.length > 500) {
                            note.content.take(500) + "..."
                        } else {
                            note.content
                        }
                        append(context.getString(R.string.prompt_note_format, index + 1, note.title, content))
                        append("\n\n")
                    }
                }
            } else {
                context.getString(R.string.prompt_empty_notes)
            }

            val systemPrompt = buildString {
                append(context.getString(R.string.prompt_system))
                append("\n\n")
                append(notesContext)
                append("\n\n")
                append(context.getString(R.string.prompt_requirements))
            }

            val messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = question)
            )

            val request = ChatRequest(messages = messages)
            val response = apiService.chat(request)

            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.message?.content
                    ?: context.getString(R.string.chat_error, "No response")
            } else {
                context.getString(R.string.chat_error, "${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("AiRepository", "Error: ${e.message}", e)
            context.getString(R.string.chat_error, e.message ?: "Unknown")
        }
    }
}