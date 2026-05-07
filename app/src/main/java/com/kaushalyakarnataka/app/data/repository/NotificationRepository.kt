package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.NotificationType
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

private const val TAG = "NotificationRepository"
private const val QUERY_LIMIT = 100L

private fun friendlyError(e: Throwable): String {
    val msg = e.message ?: ""
    return when {
        msg.contains("PERMISSION_DENIED", ignoreCase = true) ->
            "You don't have permission to access notifications."
        msg.contains("UNAVAILABLE", ignoreCase = true) || msg.contains("network", ignoreCase = true) ->
            "No internet connection. Please check your network and try again."
        msg.contains("not found", ignoreCase = true) ->
            "Notifications not found."
        else -> "Unable to load notifications right now. Please try again later."
    }
}

interface NotificationRepository {
    suspend fun getNotifications(userId: String): UiState<List<AppNotification>>
    fun observeNotifications(userId: String): Flow<UiState<List<AppNotification>>>
    suspend fun markAllRead(userId: String): UiState<Unit>
    suspend fun sendNotification(notification: AppNotification): UiState<Unit>
    suspend fun getUnreadCount(userId: String): Int
}

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val notifRef get() = firestore.collection(FirestoreCollections.NOTIFICATIONS)

    /**
     * Uses only a single-field equality filter (userId) to avoid requiring a Firestore composite index.
     * Results are sorted client-side by createdAt descending and limited to the top 50.
     */
    override suspend fun getNotifications(userId: String): UiState<List<AppNotification>> {
        Log.d(TAG, "getNotifications start userId=$userId")
        return try {
            val snapshot = notifRef
                .whereEqualTo(FirestoreCollections.Fields.USER_ID, userId)
                .limit(QUERY_LIMIT)
                .get()
                .await()
            val notifs = snapshot.documents
                .mapNotNull { doc -> parseNotification(doc.data ?: return@mapNotNull null, doc.id) }
                .sortedByDescending { it.createdAt }
                .take(50)
            Log.d(TAG, "getNotifications success count=${notifs.size} docs=${snapshot.size()}")
            UiState.Success(notifs)
        } catch (e: Exception) {
            Log.e(TAG, "getNotifications failed userId=$userId", e)
            UiState.Error(friendlyError(e))
        }
    }

    /**
     * Real-time listener using a single-field equality filter.
     * Client-side sorting avoids FAILED_PRECONDITION composite-index errors.
     */
    override fun observeNotifications(userId: String): Flow<UiState<List<AppNotification>>> = callbackFlow {
        if (userId.isBlank()) {
            Log.w(TAG, "observeNotifications blank userId")
            trySend(UiState.Success(emptyList()))
            close()
            return@callbackFlow
        }
        Log.d(TAG, "observeNotifications registering listener userId=$userId")
        val registration = notifRef
            .whereEqualTo(FirestoreCollections.Fields.USER_ID, userId)
            .limit(QUERY_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeNotifications snapshot error userId=$userId msg=${error.message}")
                    trySend(UiState.Error(friendlyError(error)))
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.w(TAG, "observeNotifications snapshot null userId=$userId")
                    trySend(UiState.Success(emptyList()))
                    return@addSnapshotListener
                }
                val notifs = snapshot.documents
                    .mapNotNull { doc -> parseNotification(doc.data ?: return@mapNotNull null, doc.id) }
                    .sortedByDescending { it.createdAt }
                    .take(50)
                val unread = notifs.count { !it.isRead }
                Log.d(TAG, "observeNotifications updated count=${notifs.size} unread=$unread userId=$userId")
                trySend(UiState.Success(notifs))
            }
        awaitClose {
            Log.d(TAG, "observeNotifications removing listener userId=$userId")
            registration.remove()
        }
    }

    /**
     * Marks notifications as read using a single-field query + client-side filtering.
     * Avoids the composite index that would be required for userId + isRead together.
     */
    override suspend fun markAllRead(userId: String): UiState<Unit> {
        Log.d(TAG, "markAllRead start userId=$userId")
        return try {
            val snapshot = notifRef
                .whereEqualTo(FirestoreCollections.Fields.USER_ID, userId)
                .limit(QUERY_LIMIT)
                .get()
                .await()
            val batch = firestore.batch()
            var updated = 0
            snapshot.documents.forEach { doc ->
                val read = doc.getBoolean(FirestoreCollections.Fields.IS_READ) ?: false
                if (!read) {
                    batch.update(doc.reference, FirestoreCollections.Fields.IS_READ, true)
                    updated++
                }
            }
            if (updated > 0) {
                batch.commit().await()
                Log.d(TAG, "markAllRead committed updated=$updated userId=$userId")
            } else {
                Log.d(TAG, "markAllRead nothing to update userId=$userId")
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "markAllRead failed userId=$userId", e)
            UiState.Error(friendlyError(e))
        }
    }

    override suspend fun sendNotification(notification: AppNotification): UiState<Unit> {
        Log.d(TAG, "sendNotification type=${notification.type} userId=${notification.userId}")
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
            Log.d(TAG, "sendNotification success id=$id")
            UiState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendNotification failed", e)
            UiState.Error("Failed to send notification. ${e.message ?: ""}")
        }
    }

    /**
     * Counts unread notifications using a single-field query + client-side filter.
     */
    override suspend fun getUnreadCount(userId: String): Int {
        Log.d(TAG, "getUnreadCount start userId=$userId")
        return try {
            val snapshot = notifRef
                .whereEqualTo(FirestoreCollections.Fields.USER_ID, userId)
                .limit(QUERY_LIMIT)
                .get()
                .await()
            val count = snapshot.documents.count { doc ->
                doc.getBoolean(FirestoreCollections.Fields.IS_READ) != true
            }
            Log.d(TAG, "getUnreadCount success count=$count userId=$userId")
            count
        } catch (e: Exception) {
            Log.e(TAG, "getUnreadCount failed userId=$userId", e)
            0
        }
    }

    private fun parseNotification(data: Map<String, Any>, docId: String): AppNotification? {
        return try {
            AppNotification(
                id = data["id"] as? String ?: docId,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                type = try { NotificationType.valueOf(data["type"] as? String ?: "BOOKING_UPDATE") } catch (e: Exception) { NotificationType.BOOKING_UPDATE },
                bookingId = data["bookingId"] as? String ?: "",
                isRead = data["isRead"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
            )
        } catch (e: Exception) {
            Log.w(TAG, "parseNotification failed docId=$docId", e)
            null
        }
    }
}
