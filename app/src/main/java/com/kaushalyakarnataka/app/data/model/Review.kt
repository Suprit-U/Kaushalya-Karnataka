package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

/**
 * Customer review for a completed booking.
 * Displayed on WorkerProfileScreen (preview) and ReviewsScreen (full list).
 */
data class Review(
    val id: String = "",
    val workerId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerInitial: String = "",    // First letter of name for avatar
    val customerAvatarUrl: String = "",
    val rating: Int = 5,                // 1–5 stars
    val comment: String = "",
    val serviceType: String = "",        // e.g. "Electrical Repair"
    val photoUrls: List<String> = emptyList(),
    val helpfulCount: Int = 0,
    val isVerified: Boolean = false,     // Verified purchase
    val createdAt: Timestamp = Timestamp.now(),
)

/**
 * Aggregated rating statistics for a worker.
 * Used in RatingBreakdown composable on ReviewsScreen.
 */
data class RatingStats(
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val ratingCounts: Map<Int, Int> = emptyMap()
)
