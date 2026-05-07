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
    suspend fun requestFinalAmount(bookingId: String, amount: Int): UiState<Unit>
    suspend fun approveCustomerFinalAmount(bookingId: String): UiState<Unit>
    suspend fun counterFinalAmount(bookingId: String, amount: Int): UiState<Unit>
    suspend fun proposeNegotiatedAmount(bookingId: String, amount: Int): UiState<Unit>
    suspend fun respondToNegotiation(bookingId: String, accepted: Boolean, finalAmount: Int): UiState<Unit>
    suspend fun completeBookingWithFinalAmount(bookingId: String, finalAmount: Int): UiState<Unit>
    suspend fun workerSendFinalAmount(bookingId: String, amount: Int): UiState<Unit>
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
            bookingsRef.document(bookingId).set(buildBookingMap(newBooking)).await()

            notificationRepository.sendNotification(
                AppNotification(
                    userId = newBooking.workerId,
                    title = "New booking request",
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
            UiState.Success(parseBookings(snapshot.documents).sortedByDescending { it.createdAt.seconds })
        } catch (e: Exception) {
            Log.e(TAG, "getCustomerBookings failed", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun getWorkerBookings(workerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef.whereEqualTo("workerId", workerId).get().await()
            UiState.Success(parseBookings(snapshot.documents).sortedBy { it.scheduledDate.seconds })
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
                .get()
                .await()
            UiState.Success(parseBookings(snapshot.documents))
        } catch (e: Exception) {
            val allResult = getWorkerBookings(workerId)
            if (allResult is UiState.Success) {
                UiState.Success(allResult.data.filter { it.status == BookingStatus.PENDING })
            } else {
                UiState.Success(emptyList())
            }
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId).update("status", status.name).await()
            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                val notification = when (status) {
                    BookingStatus.CONFIRMED -> AppNotification(
                        userId = booking.customerId,
                        title = "Booking accepted",
                        message = "${booking.workerName} accepted your booking for ${booking.service}",
                        type = NotificationType.BOOKING_CONFIRMED,
                        bookingId = bookingId
                    )
                    BookingStatus.CANCELLED -> AppNotification(
                        userId = booking.customerId,
                        title = "Booking declined",
                        message = "Your booking for ${booking.service} was declined",
                        type = NotificationType.BOOKING_DECLINED,
                        bookingId = bookingId
                    )
                    BookingStatus.COMPLETED -> AppNotification(
                        userId = booking.customerId,
                        title = "Booking completed",
                        message = "Rate your experience with ${booking.workerName}",
                        type = NotificationType.BOOKING_COMPLETED,
                        bookingId = bookingId
                    )
                    else -> null
                }
                if (notification != null) notificationRepository.sendNotification(notification)
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

    override suspend fun requestFinalAmount(bookingId: String, amount: Int): UiState<Unit> {
        if (amount <= 0) return UiState.Error("Enter a valid final amount")
        return try {
            bookingsRef.document(bookingId).update(
                mapOf(
                    "customerProposedAmount" to amount,
                    "workerCounterAmount" to 0,
                    "negotiatedAmount" to 0,
                    "negotiationStatus" to NegotiationStatus.CUSTOMER_PROPOSED.name,
                    "status" to BookingStatus.AWAITING_PAYMENT_CONFIRMATION.name
                )
            ).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.workerId,
                        title = "Final amount requested",
                        message = "${booking.customerName} requested completion at ${formatAmount(amount)}",
                        type = NotificationType.BOOKING_UPDATE,
                        bookingId = bookingId
                    )
                )
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to request final amount")
        }
    }

    override suspend fun workerSendFinalAmount(bookingId: String, amount: Int): UiState<Unit> {
        if (amount <= 0) return UiState.Error("Enter a valid final amount")
        return try {
            bookingsRef.document(bookingId).update(
                mapOf(
                    "negotiatedAmount" to amount,
                    "negotiationStatus" to NegotiationStatus.WORKER_PROPOSED.name,
                    "status" to BookingStatus.AWAITING_PAYMENT_CONFIRMATION.name
                )
            ).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.customerId,
                        title = "Final amount sent",
                        message = "${booking.workerName} sent a final amount of ${formatAmount(amount)} for ${booking.service}",
                        type = NotificationType.BOOKING_UPDATE,
                        bookingId = bookingId
                    )
                )
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to send final amount")
        }
    }

    override suspend fun approveCustomerFinalAmount(bookingId: String): UiState<Unit> {
        return try {
            val booking = (getBookingById(bookingId) as? UiState.Success)?.data
                ?: return UiState.Error("Booking not found")
            val amount = booking.customerProposedAmount
            if (amount <= 0) return UiState.Error("No customer amount to approve")

            bookingsRef.document(bookingId).update(
                mapOf(
                    "finalAmount" to amount,
                    "workerCounterAmount" to 0,
                    "negotiatedAmount" to 0,
                    "negotiationStatus" to NegotiationStatus.WORKER_APPROVED.name,
                    "status" to BookingStatus.COMPLETED.name
                )
            ).await()

            notificationRepository.sendNotification(
                AppNotification(
                    userId = booking.customerId,
                    title = "Final amount approved",
                    message = "${booking.workerName} approved ${formatAmount(amount)}. Booking completed.",
                    type = NotificationType.BOOKING_COMPLETED,
                    bookingId = bookingId
                )
            )
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to approve final amount")
        }
    }

    override suspend fun counterFinalAmount(bookingId: String, amount: Int): UiState<Unit> {
        if (amount <= 0) return UiState.Error("Enter a valid revised amount")
        return try {
            bookingsRef.document(bookingId).update(
                mapOf(
                    "workerCounterAmount" to amount,
                    "negotiatedAmount" to amount,
                    "negotiationStatus" to NegotiationStatus.WORKER_COUNTERED.name,
                    "status" to BookingStatus.AWAITING_PAYMENT_CONFIRMATION.name
                )
            ).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.customerId,
                        title = "Revised final amount",
                        message = "${booking.workerName} suggested ${formatAmount(amount)} for ${booking.service}",
                        type = NotificationType.BOOKING_UPDATE,
                        bookingId = bookingId
                    )
                )
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to suggest revised amount")
        }
    }

    override suspend fun proposeNegotiatedAmount(bookingId: String, amount: Int): UiState<Unit> {
        return counterFinalAmount(bookingId, amount)
    }

    override suspend fun respondToNegotiation(bookingId: String, accepted: Boolean, finalAmount: Int): UiState<Unit> {
        return try {
            val booking = (getBookingById(bookingId) as? UiState.Success)?.data
                ?: return UiState.Error("Booking not found")
            if (accepted) {
                val agreedAmount = when {
                    finalAmount > 0 -> finalAmount
                    booking.workerCounterAmount > 0 -> booking.workerCounterAmount
                    booking.negotiatedAmount > 0 -> booking.negotiatedAmount
                    else -> 0
                }
                if (agreedAmount <= 0) return UiState.Error("No revised amount to confirm")
                bookingsRef.document(bookingId).update(
                    mapOf(
                        "negotiationStatus" to NegotiationStatus.CUSTOMER_ACCEPTED.name,
                        "finalAmount" to agreedAmount,
                        "status" to BookingStatus.COMPLETED.name
                    )
                ).await()
            } else {
                bookingsRef.document(bookingId).update(
                    mapOf(
                        "negotiationStatus" to NegotiationStatus.CUSTOMER_REJECTED.name,
                        "status" to BookingStatus.CONFIRMED.name
                    )
                ).await()
            }

            if (accepted) {
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.workerId,
                        title = "Final amount accepted",
                        message = "Customer accepted final payment of ${formatAmount(finalAmount.takeIf { it > 0 } ?: booking.workerCounterAmount.takeIf { it > 0 } ?: booking.negotiatedAmount)}",
                        type = NotificationType.BOOKING_COMPLETED,
                        bookingId = bookingId
                    )
                )
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.customerId,
                        title = "Booking completed",
                        message = "Your booking for ${booking.service} is complete. Rate your experience with ${booking.workerName}.",
                        type = NotificationType.BOOKING_COMPLETED,
                        bookingId = bookingId
                    )
                )
            } else {
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.workerId,
                        title = "Final amount rejected",
                        message = "Customer requested clarification on the final amount. Please discuss or send a new amount.",
                        type = NotificationType.BOOKING_UPDATE,
                        bookingId = bookingId
                    )
                )
            }
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to respond to negotiation")
        }
    }

    override suspend fun completeBookingWithFinalAmount(bookingId: String, finalAmount: Int): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId).update(
                mapOf(
                    "status" to BookingStatus.COMPLETED.name,
                    "finalAmount" to finalAmount,
                    "negotiationStatus" to NegotiationStatus.NONE.name
                )
            ).await()

            val bookingResult = getBookingById(bookingId)
            if (bookingResult is UiState.Success) {
                val booking = bookingResult.data
                notificationRepository.sendNotification(
                    AppNotification(
                        userId = booking.customerId,
                        title = "Booking completed",
                        message = "Rate your experience with ${booking.workerName}",
                        type = NotificationType.BOOKING_COMPLETED,
                        bookingId = bookingId
                    )
                )
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
        "customerProposedAmount" to booking.customerProposedAmount,
        "workerCounterAmount" to booking.workerCounterAmount,
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
            estimatedCostMin = (data["estimatedCostMin"] as? Number)?.toInt() ?: 0,
            estimatedCostMax = (data["estimatedCostMax"] as? Number)?.toInt() ?: 0,
            finalAmount = (data["finalAmount"] as? Number)?.toInt() ?: 0,
            customerProposedAmount = (data["customerProposedAmount"] as? Number)?.toInt() ?: 0,
            workerCounterAmount = (data["workerCounterAmount"] as? Number)?.toInt() ?: 0,
            negotiatedAmount = (data["negotiatedAmount"] as? Number)?.toInt() ?: 0,
            negotiationStatus = try { NegotiationStatus.valueOf(data["negotiationStatus"] as? String ?: "NONE") } catch (e: Exception) { NegotiationStatus.NONE },
            bookingCode = data["bookingCode"] as? String ?: "",
            couponCode = data["couponCode"] as? String ?: "",
            discountAmount = (data["discountAmount"] as? Number)?.toInt() ?: 0,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
        )
    }

    private fun formatAmount(amount: Int): String = "Rs. $amount"
}
