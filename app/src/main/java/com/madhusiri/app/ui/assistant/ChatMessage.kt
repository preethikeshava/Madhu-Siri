package com.madhusiri.app.ui.assistant

data class ChatMessage(
    val text: String,
    val isUser: Boolean, // true if from user, false if from AI
    val timestamp: Long = System.currentTimeMillis()
)
