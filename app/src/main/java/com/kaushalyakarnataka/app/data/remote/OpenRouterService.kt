package com.kaushalyakarnataka.app.data.remote

import com.kaushalyakarnataka.app.BuildConfig
import com.kaushalyakarnataka.app.data.model.openrouter.OpenRouterRequest
import com.kaushalyakarnataka.app.data.model.openrouter.OpenRouterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterService @Inject constructor() {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun generateSummary(reviewsText: String): Result<String> {
        return try {
            val request = OpenRouterRequest(
                model = "openai/gpt-oss-120b",
                messages = listOf(
                    com.kaushalyakarnataka.app.data.model.openrouter.Message(
                        role = "system",
                        content = "You are a helpful assistant that summarizes worker reviews into short, natural, human-readable insights. Keep summaries to 1-3 lines. Only mention patterns actually present in the reviews. Do not hallucinate."
                    ),
                    com.kaushalyakarnataka.app.data.model.openrouter.Message(
                        role = "user",
                        content = "Summarize these worker reviews into a short insight (1-3 lines max):\n\n$reviewsText"
                    )
                )
            )

            val response: OpenRouterResponse = client.post("https://openrouter.ai/api/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                    append("HTTP-Referer", "https://kaushalyakarnataka.app")
                    append("X-Title", "Kaushalya Karnataka")
                }
                setBody(request)
            }.body()

            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "API error"))
            } else {
                val summary = response.choices?.firstOrNull()?.message?.content?.trim()
                if (!summary.isNullOrBlank()) {
                    Result.success(summary)
                } else {
                    Result.failure(Exception("Empty summary received"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
