package com.kaushalyakarnataka.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.remote.OpenRouterService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiSummaryDataStore by preferencesDataStore(name = "ai_summary_cache")

interface AiSummaryRepository {
    suspend fun getSummary(workerId: String, reviews: List<Review>): Result<String>
    fun getCachedSummary(workerId: String): Flow<String?>
    suspend fun clearCache(workerId: String)
}

@Singleton
class AiSummaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openRouterService: OpenRouterService
) : AiSummaryRepository {

    private val dataStore = context.aiSummaryDataStore

    companion object {
        private fun summaryKey(workerId: String) = stringPreferencesKey("summary_$workerId")
        private fun timestampKey(workerId: String) = longPreferencesKey("summary_ts_$workerId")
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    override suspend fun getSummary(workerId: String, reviews: List<Review>): Result<String> {
        // Check cache first
        val cached = getCachedSummary(workerId).first()
        if (!cached.isNullOrBlank()) {
            // Verify cache is not too old
            val ts = dataStore.data.map { it[timestampKey(workerId)] ?: 0L }.first()
            if (System.currentTimeMillis() - ts < CACHE_DURATION_MS) {
                return Result.success(cached)
            }
        }

        if (reviews.size < 2) {
            return Result.failure(Exception("Not enough reviews"))
        }

        // Build prompt from real review data
        val reviewsText = reviews.joinToString("\n---\n") { review ->
            val stars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
            "[$stars] ${review.comment}"
        }

        // Call API
        val result = openRouterService.generateSummary(reviewsText)

        // Cache on success
        result.getOrNull()?.let { summary ->
            dataStore.edit { prefs ->
                prefs[summaryKey(workerId)] = summary
                prefs[timestampKey(workerId)] = System.currentTimeMillis()
            }
        }

        return result
    }

    override fun getCachedSummary(workerId: String): Flow<String?> {
        return dataStore.data.map { it[summaryKey(workerId)] }
    }

    override suspend fun clearCache(workerId: String) {
        dataStore.edit { prefs ->
            prefs.remove(summaryKey(workerId))
            prefs.remove(timestampKey(workerId))
        }
    }
}
