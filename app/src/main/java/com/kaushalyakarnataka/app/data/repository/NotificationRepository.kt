package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.NotificationType
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

private const val TAG = "NotificationRepository"

interface NotificationRepository {
    suspend fun getNotifications(userId: String): UiState<List<AppNotification>>
    suspend fun markAllRead(userId: String): UiState<Unit>
    suspend fun sendNotification(notification: AppNotification): UiState<Unit>
    suspend fun getUnreadCount(userId: String): Int
}

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val notifRef get() = firestore.collection("notifications")

    override suspend fun getNotifications(userId: String): UiState<List<AppNotification>> {
        return try {
            val snapshot = notifRef
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val notifs = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    AppNotification(
                        id = data["id"] as? String ?: doc.id,
                        userId = data["userId"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        message = data["message"] as? String ?: "",
                        type = try { NotificationType.valueOf(data["type"] as? String ?: "BOOKING_UPDATE") } catch (e: Exception) { NotificationType.BOOKING_UPDATE },
                        bookingId = data["bookingId"] as? String ?: "",
                        isRead = data["isRead"] as? Boolean ?: false,
                        createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
                    )
                } catch (e: Exception) { null }
            }
            UiState.Success(notifs)
        } catch (e: Exception) {
            Log.e(TAG, "getNotifications failed", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun markAllRead(userId: String): UiState<Unit> {
        return try {
            val snapshot = notifRef.whereEqualTo("userId", userId).whereEqualTo("isRead", false).get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed")
        }
    }

    override suspend fun sendNotification(notification: AppNotification): UiState<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val notif = notification.copy(id = id, createdAt = Timestamp.now())
            val data = mapOf(
                "id" to notif.id,
                "userId" to notif.userId,
                "title" to notif.title,
                "message" to notif.message,
                "type" to notif.type.name,
                "bookingId" to notif.bookingId,
                "isRead" to notif.isRead,
                "createdAt" to notif.createdAt
            )
            notifRef.document(id).set(data).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendNotification failed", e)
            UiState.Error(e.message ?: "Failed to send notification")
        }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = notifRef.whereEqualTo("userId", userId).whereEqualTo("isRead", false).get().await()
            snapshot.size()
        } catch (e: Exception) { 0 }
    }
}
