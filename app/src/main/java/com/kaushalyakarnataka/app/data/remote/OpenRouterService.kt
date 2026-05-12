package com.kaushalyakarnataka.app.data.remote

import android.util.Log
import com.kaushalyakarnataka.app.BuildConfig
import com.kaushalyakarnataka.app.data.model.openrouter.OpenRouterRequest
import com.kaushalyakarnataka.app.data.model.openrouter.OpenRouterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "OpenRouterService"

@Singleton
class OpenRouterService @Inject constructor() {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
    }

    suspend fun generateSummary(reviewsText: String): Result<String> {
        if (BuildConfig.OPENROUTER_API_KEY.isBlank()) {
            Log.e(TAG, "OPENROUTER_API_KEY is blank. Add it to local.properties.")
            return Result.failure(Exception("API key not configured. Add OPENROUTER_API_KEY to local.properties."))
        }

        return try {
            val request = buildRequest(reviewsText)
            Log.d(TAG, "Sending request to OpenRouter with model=${request.model}")

            val httpResponse = client.post("https://openrouter.ai/api/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                    append("HTTP-Referer", "https://kaushalyakarnataka.app")
                    append("X-Title", "Kaushalya Karnataka")
                }
                setBody(request)
            }

            Log.d(TAG, "OpenRouter response status: ${httpResponse.status}")

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    val response: OpenRouterResponse = httpResponse.body()
                    parseResponse(response)
                }
                HttpStatusCode.TooManyRequests -> {
                    Log.w(TAG, "Rate limited by OpenRouter")
                    Result.failure(Exception("Rate limited. Please try again later."))
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e(TAG, "OpenRouter returned 401 — API key invalid or missing")
                    Result.failure(Exception("Invalid API key"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorBody = try {
                        httpResponse.body<OpenRouterResponse>()
                    } catch (_: Exception) { null }
                    val errMsg = errorBody?.error?.message ?: httpResponse.status.description
                    Log.e(TAG, "OpenRouter BadRequest: $errMsg")
                    Result.failure(Exception("Bad request: $errMsg"))
                }
                else -> {
                    val body = try { httpResponse.body<String>() } catch (_: Exception) { "" }
                    Log.e(TAG, "OpenRouter error ${httpResponse.status}: $body")
                    Result.failure(Exception("API error: ${httpResponse.status} ${httpResponse.status.description}"))
                }
            }
        } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
            Log.e(TAG, "Request timed out", e)
            Result.failure(Exception("Request timed out. Please check your connection."))
        } catch (e: Exception) {
            Log.e(TAG, "Network error", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }

    private fun buildRequest(reviewsText: String): OpenRouterRequest {
        return OpenRouterRequest(
            model = "openai/gpt-oss-120b:free",
            messages = listOf(
                com.kaushalyakarnataka.app.data.model.openrouter.Message(
                    role = "system",
                    content = SYSTEM_PROMPT
                ),
                com.kaushalyakarnataka.app.data.model.openrouter.Message(
                    role = "user",
                    content = "Summarize these worker reviews into a short insight (1-3 lines max):\n\n$reviewsText"
                )
            )
        )
    }

    private fun parseResponse(response: OpenRouterResponse): Result<String> {
        // Handle explicit API error in response body
        if (response.error != null) {
            val msg = response.error.message ?: "Unknown API error"
            return Result.failure(Exception("API error: $msg"))
        }

        // Validate choices array
        val choices = response.choices
        if (choices.isNullOrEmpty()) {
            return Result.failure(Exception("No response from AI model"))
        }

        // Extract content from first choice
        val firstChoice = choices[0]
        val messageContent = firstChoice.message?.content

        return if (!messageContent.isNullOrBlank()) {
            val cleaned = messageContent.trim()
            if (cleaned.length > 10) {
                Result.success(cleaned)
            } else {
                Result.failure(Exception("Response too short"))
            }
        } else {
            Result.failure(Exception("Empty response from AI model"))
        }
    }

    companion object {
        private val SYSTEM_PROMPT = """
            You are a helpful assistant that summarizes worker reviews.
            Rules:
            - Summarize into 1-3 short lines maximum
            - Use natural, human-readable language
            - Only mention patterns actually present in the reviews
            - Do not hallucinate or invent details
            - Focus on what customers praise or frequently mention
            - Example: "Customers frequently praise this worker for punctuality, clean work, and professional behavior."
        """.trimIndent()
    }
}
