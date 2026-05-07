package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.NotificationType
import com.kaushalyakarnataka.app.data.model.RatingStats
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

private const val TAG = "ReviewRepository"

interface ReviewRepository {
    suspend fun getWorkerReviews(workerId: String): UiState<List<Review>>
    fun observeWorkerReviews(workerId: String): Flow<UiState<List<Review>>>
    suspend fun getWorkerRatingStats(workerId: String): UiState<RatingStats>
    suspend fun addReview(review: Review): UiState<Review>
    suspend fun markHelpful(reviewId: String): UiState<Unit>
    suspend fun hasUserReviewedBooking(bookingId: String, customerId: String): Boolean
}

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
) : ReviewRepository {

    private val reviewsRef get() = firestore.collection(FirestoreCollections.REVIEWS)

    override suspend fun getWorkerReviews(workerId: String): UiState<List<Review>> {
        return try {
            val snapshot = reviewsRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                parseReviewData(doc.data ?: return@mapNotNull null, doc.id)
            }.sortedByDescending { it.createdAt.seconds }
            UiState.Success(reviews)
        } catch (e: Exception) {
            Log.e(TAG, "getWorkerReviews failed", e)
            // Return empty list - no fake data
            UiState.Success(emptyList())
        }
    }

    override fun observeWorkerReviews(workerId: String): Flow<UiState<List<Review>>> = callbackFlow {
        if (workerId.isBlank()) {
            trySend(UiState.Success(emptyList()))
            close()
            return@callbackFlow
        }
        val registration = reviewsRef
            .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to load reviews"))
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents
                    ?.mapNotNull { doc -> parseReviewData(doc.data ?: return@mapNotNull null, doc.id) }
                    ?.sortedByDescending { it.createdAt.seconds }
                    ?: emptyList()
                trySend(UiState.Success(reviews))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getWorkerRatingStats(workerId: String): UiState<RatingStats> {
        return try {
            val reviewsResult = getWorkerReviews(workerId)
            val reviews = (reviewsResult as? UiState.Success)?.data ?: emptyList()
            if (reviews.isEmpty()) {
                return UiState.Success(RatingStats(0f, 0, emptyMap()))
            }
            val stats = RatingStats(
                averageRating = reviews.map { it.rating }.average().coerceAtLeast(0.0).toFloat(),
                totalReviews = reviews.size,
                ratingCounts = (1..5).associateWith { rating -> reviews.count { it.rating == rating } },
            )
            UiState.Success(stats)
        } catch (e: Exception) {
            UiState.Success(RatingStats(0f, 0, emptyMap()))
        }
    }

    override suspend fun addReview(review: Review): UiState<Review> {
        return try {
            if (review.bookingId.isNotBlank() && hasUserReviewedBooking(review.bookingId, review.customerId)) {
                return UiState.Error("You have already reviewed this booking")
            }
            val reviewId = when {
                review.id.isNotBlank() -> review.id
                review.bookingId.isNotBlank() -> "${review.bookingId}_${review.customerId}"
                else -> UUID.randomUUID().toString()
            }
            val newReview = review.copy(id = reviewId, createdAt = Timestamp.now())
            val reviewMap = mapOf(
                "id" to newReview.id,
                "workerId" to newReview.workerId,
                "customerId" to newReview.customerId,
                "customerName" to newReview.customerName,
                "customerInitial" to newReview.customerInitial,
                "customerAvatarUrl" to newReview.customerAvatarUrl,
                "rating" to newReview.rating,
                "comment" to newReview.comment,
                "serviceType" to newReview.serviceType,
                "photoUrls" to newReview.photoUrls,
                "helpfulCount" to newReview.helpfulCount,
                "isVerified" to newReview.isVerified,
                "bookingId" to newReview.bookingId,
                "createdAt" to newReview.createdAt,
            )
            reviewsRef.document(reviewId).set(reviewMap).await()

            updateWorkerRating(newReview.workerId)
            notificationRepository.sendNotification(
                AppNotification(
                    userId = newReview.workerId,
                    title = "New review received",
                    message = "${newReview.customerName} rated ${newReview.rating}/5 for ${newReview.serviceType}",
                    type = NotificationType.NEW_REVIEW,
                    bookingId = newReview.bookingId
                )
            )

            UiState.Success(newReview)
        } catch (e: Exception) {
            Log.e(TAG, "addReview failed", e)
            UiState.Error(e.message ?: "Failed to submit review")
        }
    }

    private suspend fun updateWorkerRating(workerId: String) {
        try {
            val snapshot = reviewsRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                parseReviewData(doc.data ?: return@mapNotNull null, doc.id)
            }
            if (reviews.isEmpty()) return
            val avg = reviews.map { it.rating }.average()
            firestore.collection(FirestoreCollections.WORKERS).document(workerId)
                .update(mapOf(
                    "rating" to avg,
                    "reviewCount" to reviews.size
                )).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update worker rating: ${e.message}")
        }
    }

    override suspend fun markHelpful(reviewId: String): UiState<Unit> {
        return try {
            val snapshot = reviewsRef.document(reviewId).get().await()
            val currentCount = snapshot.getLong("helpfulCount") ?: 0
            reviewsRef.document(reviewId).update("helpfulCount", currentCount + 1).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to mark helpful")
        }
    }

    override suspend fun hasUserReviewedBooking(bookingId: String, customerId: String): Boolean {
        return try {
            val snapshot = reviewsRef
                .whereEqualTo("bookingId", bookingId)
                .whereEqualTo("customerId", customerId)
                .get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private fun parseReviewData(data: Map<String, Any>, docId: String): Review? {
        return try {
            Review(
                id = data["id"] as? String ?: docId,
                workerId = data["workerId"] as? String ?: "",
                customerId = data["customerId"] as? String ?: "",
                customerName = data["customerName"] as? String ?: "",
                customerInitial = data["customerInitial"] as? String ?: "",
                customerAvatarUrl = data["customerAvatarUrl"] as? String ?: "",
                rating = ((data["rating"] as? Number)?.toInt() ?: 5).coerceIn(1, 5),
                comment = data["comment"] as? String ?: "",
                serviceType = data["serviceType"] as? String ?: "",
                photoUrls = (data["photoUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                helpfulCount = (data["helpfulCount"] as? Number)?.toInt() ?: 0,
                isVerified = data["isVerified"] as? Boolean ?: false,
                bookingId = data["bookingId"] as? String ?: "",
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse review $docId", e)
            null
        }
    }
}
