package com.example.lightmind.ui.chat

import android.app.Application
import android.util.Log.e
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lightmind.data.local.NoteDatabase
import com.example.lightmind.data.remote.AiRepository
import kotlinx.coroutines.launch
import com.example.lightmind.R

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val aiRepository = AiRepository(application.applicationContext, noteDao)

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _chatMessages.value = listOf(
            ChatMessage(
                content = application.getString(R.string.chat_welcome),
                isUser = false
            )
        )
    }

    fun askQuestion(question: String) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)

                val currentMessages = _chatMessages.value.orEmpty().toMutableList()
                currentMessages.add(ChatMessage(question, true))
                _chatMessages.postValue(currentMessages)

                val answer = aiRepository.askQuestion(question)

                currentMessages.add(ChatMessage(answer, false))
                _chatMessages.postValue(currentMessages)

            } catch (e: Exception) {
                e.printStackTrace()
                val currentMessages = _chatMessages.value.orEmpty().toMutableList()
                currentMessages.add(
                    ChatMessage(
                        getApplication<Application>().getString(
                            R.string.chat_error_generic,
                            e.message
                        ),
                        false
                    )
                )
                _chatMessages.postValue(currentMessages)
            }finally {
                _isLoading.postValue(false)
            }
        }
    }
}