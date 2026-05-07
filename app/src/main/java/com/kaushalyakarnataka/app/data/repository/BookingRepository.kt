package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.NotificationType
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

private const val TAG = "BookingRepository"

interface BookingRepository {
    suspend fun createBooking(booking: Booking): UiState<Booking>
    suspend fun getCustomerBookings(customerId: String): UiState<List<Booking>>
    suspend fun getWorkerBookings(workerId: String): UiState<List<Booking>>
    suspend fun getWorkerPendingBookings(workerId: String): UiState<List<Booking>>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): UiState<Unit>
    suspend fun getBookingById(bookingId: String): UiState<Booking>
}

class BookingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) : BookingRepository {

    private val bookingsRef get() = firestore.collection(FirestoreCollections.BOOKINGS)

    override suspend fun createBooking(booking: Booking): UiState<Booking> {
        return try {
            val bookingId = UUID.randomUUID().toString()
            val bookingCode = "#KK-${System.currentTimeMillis() % 100000}"
            val newBooking = booking.copy(
                id = bookingId,
                bookingCode = bookingCode,
                status = BookingStatus.PENDING,
                createdAt = Timestamp.now(),
            )
            val bookingMap = mapOf(
                "id" to newBooking.id,
                "customerId" to newBooking.customerId,
                "customerName" to newBooking.customerName,
                "workerId" to newBooking.workerId,
                "workerName" to newBooking.workerName,
                "service" to newBooking.service,
                "scheduledDate" to newBooking.scheduledDate,
                "timeSlot" to newBooking.timeSlot,
                "address" to newBooking.address,
                "notes" to newBooking.notes,
                "status" to newBooking.status.name,
                "estimatedCostMin" to newBooking.estimatedCostMin,
                "estimatedCostMax" to newBooking.estimatedCostMax,
                "bookingCode" to newBooking.bookingCode,
                "couponCode" to newBooking.couponCode,
                "discountAmount" to newBooking.discountAmount,
                "createdAt" to newBooking.createdAt,
            )
            bookingsRef.document(bookingId).set(bookingMap).await()

            // Notify worker of new booking request
            notificationRepository.sendNotification(
                AppNotification(
                    userId = newBooking.workerId,
                    title = "New Booking Request! 🔔",
                    message = "${newBooking.customerName} has requested ${newBooking.service} on ${newBooking.timeSlot}",
                    type = NotificationType.BOOKING_REQUEST,
                    bookingId = bookingId
                )
            )

            Log.i(TAG, "Booking created: $bookingId")
            UiState.Success(newBooking)
        } catch (e: Exception) {
            Log.e(TAG, "createBooking failed", e)
            UiState.Error(e.message ?: "Failed to create booking")
        }
    }

    override suspend fun getCustomerBookings(customerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef.whereEqualTo("customerId", customerId).get().await()
            val bookings = parseBookings(snapshot.documents).sortedByDescending { it.createdAt.seconds }
            UiState.Success(bookings)
        } catch (e: Exception) {
            Log.e(TAG, "getCustomerBookings failed", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun getWorkerBookings(workerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef.whereEqualTo("workerId", workerId).get().await()
            val bookings = parseBookings(snapshot.documents).sortedBy { it.scheduledDate.seconds }
            UiState.Success(bookings)
        } catch (e: Exception) {
            Log.e(TAG, "getWorkerBookings failed", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun getWorkerPendingBookings(workerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", BookingStatus.PENDING.name)
                .get().await()
            UiState.Success(parseBookings(snapshot.documents))
        } catch (e: Exception) {
            val allResult = getWorkerBookings(workerId)
            if (allResult is UiState.Success) {
                UiState.Success(allResult.data.filter { it.status == BookingStatus.PENDING })
            } else UiState.Success(emptyList())
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId).update("status", status.name).await()

            // Fetch booking to get details for notification
            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                val (title, message, notifType, targetUserId) = when (status) {
                    BookingStatus.CONFIRMED -> Quad(
                        "Booking Confirmed! ✅",
                        "${booking.workerName} has accepted your booking for ${booking.service}",
                        NotificationType.BOOKING_CONFIRMED,
                        booking.customerId
                    )
                    BookingStatus.CANCELLED -> Quad(
                        "Booking Declined",
                        "Your booking for ${booking.service} was declined",
                        NotificationType.BOOKING_DECLINED,
                        booking.customerId
                    )
                    BookingStatus.COMPLETED -> Quad(
                        "Job Completed! 🎉",
                        "${booking.workerName} has marked ${booking.service} as complete. Please confirm.",
                        NotificationType.BOOKING_COMPLETED,
                        booking.customerId
                    )
                    else -> null
                }
                if (title != null && targetUserId.isNotBlank()) {
                    notificationRepository.sendNotification(
                        AppNotification(
                            userId = targetUserId,
                            title = title,
                            message = message,
                            type = notifType,
                            bookingId = bookingId
                        )
                    )
                }
            }

            Log.i(TAG, "Booking $bookingId updated to ${status.name}")
            UiState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateBookingStatus failed", e)
            UiState.Error(e.message ?: "Failed to update booking status")
        }
    }

    override suspend fun getBookingById(bookingId: String): UiState<Booking> {
        return try {
            val snapshot = bookingsRef.document(bookingId).get().await()
            val data = snapshot.data ?: return UiState.Error("Booking not found")
            UiState.Success(parseBookingData(data, bookingId))
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load booking")
        }
    }

    private fun parseBookings(docs: List<com.google.firebase.firestore.DocumentSnapshot>): List<Booking> {
        return docs.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null
                parseBookingData(data, doc.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse booking ${doc.id}", e)
                null
            }
        }
    }

    private fun parseBookingData(data: Map<String, Any>, docId: String): Booking {
        return Booking(
            id = data["id"] as? String ?: docId,
            customerId = data["customerId"] as? String ?: "",
            customerName = data["customerName"] as? String ?: "",
            workerId = data["workerId"] as? String ?: "",
            workerName = data["workerName"] as? String ?: "",
            service = data["service"] as? String ?: "",
            scheduledDate = data["scheduledDate"] as? Timestamp ?: Timestamp.now(),
            timeSlot = data["timeSlot"] as? String ?: "",
            address = data["address"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            status = try { BookingStatus.valueOf(data["status"] as? String ?: "PENDING") } catch (e: Exception) { BookingStatus.PENDING },
            estimatedCostMin = (data["estimatedCostMin"] as? Long)?.toInt() ?: 0,
            estimatedCostMax = (data["estimatedCostMax"] as? Long)?.toInt() ?: 0,
            bookingCode = data["bookingCode"] as? String ?: "",
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun sampleBookings() = listOf(
    Booking(
        id = "booking1",
        customerId = "customer1",
        customerName = "Anjali Sharma",
        workerId = "worker1",
        workerName = "Ramesh Kumar",
        service = "Wiring & Repair",
        timeSlot = "9:00 AM",
        address = "42, 5th Cross, Koramangala 4th Block",
        status = BookingStatus.PENDING,
        estimatedCostMin = 450,
        estimatedCostMax = 900,
        bookingCode = "#KK-2024-0106",
    ),
    Booking(
        id = "booking2",
        customerId = "customer2",
        customerName = "Prakash Nair",
        workerId = "worker1",
        workerName = "Ramesh Kumar",
        service = "Fan Installation × 2",
        timeSlot = "2:00 PM",
        address = "HSR Layout, Sector 6",
        status = BookingStatus.CONFIRMED,
        estimatedCostMin = 700,
        estimatedCostMax = 1000,
        bookingCode = "#KK-2024-0107",
    ),
)
