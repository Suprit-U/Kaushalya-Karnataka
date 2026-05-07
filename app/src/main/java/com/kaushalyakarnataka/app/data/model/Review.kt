package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

/**
 * Customer review for a completed booking.
 */
data class Review(
    val id: String = "",
    val workerId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerInitial: String = "",
    val customerAvatarUrl: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val serviceType: String = "",
    val photoUrls: List<String> = emptyList(),
    val helpfulCount: Int = 0,
    val isVerified: Boolean = false,
    val bookingId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)

/**
 * Aggregated rating statistics for a worker.
 */
data class RatingStats(
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val ratingCounts: Map<Int, Int> = emptyMap()
)
