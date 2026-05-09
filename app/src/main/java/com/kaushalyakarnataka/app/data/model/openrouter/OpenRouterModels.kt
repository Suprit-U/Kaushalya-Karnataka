package com.kaushalyakarnataka.app.data.model.openrouter

import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>? = null,
    val error: OpenRouterError? = null
)

@Serializable
data class Choice(
    val message: MessageContent? = null
)

@Serializable
data class MessageContent(
    val content: String? = null
)

@Serializable
data class OpenRouterError(
    val message: String? = null,
    val code: Int? = null
)
