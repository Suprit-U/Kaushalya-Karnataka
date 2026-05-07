package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.NegotiationStatus
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
    suspend fun proposeNegotiatedAmount(bookingId: String, amount: Int): UiState<Unit>
    suspend fun respondToNegotiation(bookingId: String, accepted: Boolean, finalAmount: Int): UiState<Unit>
    suspend fun completeBookingWithFinalAmount(bookingId: String, finalAmount: Int): UiState<Unit>
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
            val bookingMap = buildBookingMap(newBooking)
            bookingsRef.document(bookingId).set(bookingMap).await()

            notificationRepository.sendNotification(
                AppNotification(
                    userId = newBooking.workerId,
                    title = "New Booking Request! 🔔",
                    message = "${newBooking.customerName} requested ${newBooking.service} at ${newBooking.timeSlot}",
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
            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                val notifData = when (status) {
                    BookingStatus.CONFIRMED -> Triple(
                        "Booking Confirmed! ✅",
                        "${booking.workerName} accepted your booking for ${booking.service}",
                        booking.customerId
                    )
                    BookingStatus.CANCELLED -> Triple(
                        "Booking Declined",
                        "Your booking for ${booking.service} was declined",
                        booking.customerId
                    )
                    BookingStatus.COMPLETED -> Triple(
                        "Job Completed! 🎉",
                        "Rate your experience with ${booking.workerName}",
                        booking.customerId
                    )
                    else -> null
                }
                if (notifData != null) {
                    val (title, msg, target) = notifData
                    notificationRepository.sendNotification(AppNotification(
                        userId = target, title = title, message = msg,
                        type = NotificationType.BOOKING_CONFIRMED, bookingId = bookingId
                    ))
                }
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
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

    override suspend fun proposeNegotiatedAmount(bookingId: String, amount: Int): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId).update(mapOf(
                "negotiatedAmount" to amount,
                "negotiationStatus" to NegotiationStatus.WORKER_PROPOSED.name,
                "status" to BookingStatus.AWAITING_PAYMENT_CONFIRMATION.name
            )).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(AppNotification(
                    userId = booking.customerId,
                    title = "Final Price Proposed 💰",
                    message = "${booking.workerName} proposed ₹$amount for ${booking.service}. Please confirm.",
                    type = NotificationType.BOOKING_UPDATE,
                    bookingId = bookingId
                ))
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to propose amount")
        }
    }

    override suspend fun respondToNegotiation(bookingId: String, accepted: Boolean, finalAmount: Int): UiState<Unit> {
        return try {
            if (accepted) {
                bookingsRef.document(bookingId).update(mapOf(
                    "negotiationStatus" to NegotiationStatus.CUSTOMER_ACCEPTED.name,
                    "finalAmount" to finalAmount,
                    "status" to BookingStatus.COMPLETED.name
                )).await()
            } else {
                bookingsRef.document(bookingId).update(mapOf(
                    "negotiationStatus" to NegotiationStatus.CUSTOMER_REJECTED.name,
                    "status" to BookingStatus.CONFIRMED.name
                )).await()
            }

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(AppNotification(
                    userId = booking.workerId,
                    title = if (accepted) "Payment Confirmed ✅" else "Customer Rejected Proposed Price",
                    message = if (accepted) "Customer confirmed final payment of ₹$finalAmount"
                              else "Customer rejected the proposed price. Please discuss.",
                    type = NotificationType.BOOKING_UPDATE,
                    bookingId = bookingId
                ))
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to respond to negotiation")
        }
    }

    override suspend fun completeBookingWithFinalAmount(bookingId: String, finalAmount: Int): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId).update(mapOf(
                "status" to BookingStatus.COMPLETED.name,
                "finalAmount" to finalAmount,
                "negotiationStatus" to NegotiationStatus.NONE.name
            )).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(AppNotification(
                    userId = booking.customerId,
                    title = "Job Completed! 🎉",
                    message = "Rate your experience with ${booking.workerName}",
                    type = NotificationType.BOOKING_COMPLETED,
                    bookingId = bookingId
                ))
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to complete booking")
        }
    }

    private fun buildBookingMap(booking: Booking): Map<String, Any?> = mapOf(
        "id" to booking.id,
        "customerId" to booking.customerId,
        "customerName" to booking.customerName,
        "workerId" to booking.workerId,
        "workerName" to booking.workerName,
        "service" to booking.service,
        "serviceId" to booking.serviceId,
        "scheduledDate" to booking.scheduledDate,
        "timeSlot" to booking.timeSlot,
        "address" to booking.address,
        "notes" to booking.notes,
        "status" to booking.status.name,
        "estimatedCostMin" to booking.estimatedCostMin,
        "estimatedCostMax" to booking.estimatedCostMax,
        "finalAmount" to booking.finalAmount,
        "negotiatedAmount" to booking.negotiatedAmount,
        "negotiationStatus" to booking.negotiationStatus.name,
        "bookingCode" to booking.bookingCode,
        "couponCode" to booking.couponCode,
        "discountAmount" to booking.discountAmount,
        "createdAt" to booking.createdAt,
    )

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
            serviceId = data["serviceId"] as? String ?: "",
            scheduledDate = data["scheduledDate"] as? Timestamp ?: Timestamp.now(),
            timeSlot = data["timeSlot"] as? String ?: "",
            address = data["address"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            status = try { BookingStatus.valueOf(data["status"] as? String ?: "PENDING") } catch (e: Exception) { BookingStatus.PENDING },
            estimatedCostMin = (data["estimatedCostMin"] as? Long)?.toInt() ?: 0,
            estimatedCostMax = (data["estimatedCostMax"] as? Long)?.toInt() ?: 0,
            finalAmount = (data["finalAmount"] as? Long)?.toInt() ?: 0,
            negotiatedAmount = (data["negotiatedAmount"] as? Long)?.toInt() ?: 0,
            negotiationStatus = try { NegotiationStatus.valueOf(data["negotiationStatus"] as? String ?: "NONE") } catch (e: Exception) { NegotiationStatus.NONE },
            bookingCode = data["bookingCode"] as? String ?: "",
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
        )
    }
}
