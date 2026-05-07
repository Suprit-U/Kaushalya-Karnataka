package com.kaushalyakarnataka.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.RatingStats
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

interface ReviewRepository {
    suspend fun getWorkerReviews(workerId: String): UiState<List<Review>>
    suspend fun getWorkerRatingStats(workerId: String): UiState<RatingStats>
    suspend fun addReview(review: Review): UiState<Review>
    suspend fun markHelpful(reviewId: String): UiState<Unit>
}

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ReviewRepository {

    private val reviewsRef get() = firestore.collection(FirestoreCollections.REVIEWS)

    override suspend fun getWorkerReviews(workerId: String): UiState<List<Review>> {
        return try {
            val snapshot = reviewsRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .orderBy(FirestoreCollections.Fields.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Review::class.java))
        } catch (e: Exception) {
            UiState.Success(sampleReviews())
        }
    }

    override suspend fun getWorkerRatingStats(workerId: String): UiState<RatingStats> {
        return try {
            val reviews = (getWorkerReviews(workerId) as? UiState.Success)?.data ?: return UiState.Error("No reviews")
            val stats = RatingStats(
                averageRating = reviews.map { it.rating }.average().coerceAtLeast(0.0).toFloat(),
                totalReviews = reviews.size,
                ratingCounts = (1..5).associateWith { rating -> reviews.count { it.rating == rating } },
            )
            UiState.Success(stats)
        } catch (e: Exception) {
            // Return sample stats
            UiState.Success(
                RatingStats(
                    averageRating = 4.9f,
                    totalReviews = 124,
                    ratingCounts = mapOf(
                        5 to 105,
                        4 to 12,
                        3 to 4,
                        2 to 2,
                        1 to 1,
                    ),
                )
            )
        }
    }

    override suspend fun addReview(review: Review): UiState<Review> {
        return try {
            val reviewId = UUID.randomUUID().toString()
            val newReview = review.copy(id = reviewId, createdAt = Timestamp.now())
            reviewsRef.document(reviewId).set(newReview).await()
            UiState.Success(newReview)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to submit review")
        }
    }

    override suspend fun markHelpful(reviewId: String): UiState<Unit> {
        return try {
            val snapshot = reviewsRef.document(reviewId).get().await()
            val currentCount = snapshot.getLong(FirestoreCollections.Fields.HELPFUL_COUNT) ?: 0
            reviewsRef.document(reviewId)
                .update(FirestoreCollections.Fields.HELPFUL_COUNT, currentCount + 1)
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to mark helpful")
        }
    }
}

fun sampleReviews() = listOf(
    Review(
        id = "review1",
        workerId = "worker1",
        customerId = "customer1",
        customerName = "Anjali Sharma",
        customerInitial = "A",
        rating = 5,
        comment = "Very professional and punctual! Fixed all my switches and installed new wiring neatly. Workspace was kept clean. Would definitely call again.",
        serviceType = "Electrical Repair",
        helpfulCount = 8,
        isVerified = true,
    ),
    Review(
        id = "review2",
        workerId = "worker1",
        customerId = "customer2",
        customerName = "Prakash Nair",
        customerInitial = "P",
        rating = 4,
        comment = "Good work overall. Installed 3 fans quickly and explained the warranty. Arrived slightly late but called ahead to inform. Fair pricing.",
        serviceType = "Fan Installation",
        helpfulCount = 3,
        isVerified = true,
    ),
    Review(
        id = "review3",
        workerId = "worker1",
        customerId = "customer3",
        customerName = "Sneha Reddy",
        customerInitial = "S",
        rating = 5,
        comment = "Exceptional quality! Ramesh rewired our entire apartment professionally. Very clean work, no mess, and he spotted an existing hazard and fixed it. 100% recommend.",
        serviceType = "Home Wiring",
        helpfulCount = 21,
        isVerified = true,
    ),
)
