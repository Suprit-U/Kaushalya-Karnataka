package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

/**
 * Represents a service booking between a customer and a worker.
 */
data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val service: String = "",
    val serviceId: String = "",
    val scheduledDate: Timestamp = Timestamp.now(),
    val timeSlot: String = "",
    val address: String = "",
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val estimatedCostMin: Int = 0,
    val estimatedCostMax: Int = 0,
    val finalAmount: Int = 0,          // Confirmed final paid amount
    val customerProposedAmount: Int = 0,
    val workerCounterAmount: Int = 0,
    val negotiatedAmount: Int = 0,     // Worker-proposed final amount
    val negotiationStatus: NegotiationStatus = NegotiationStatus.NONE,
    val bookingCode: String = "",
    val couponCode: String = "",
    val discountAmount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    AWAITING_PAYMENT_CONFIRMATION,
    COMPLETED,
    CANCELLED,
}

enum class NegotiationStatus {
    NONE,
    CUSTOMER_PROPOSED,
    WORKER_APPROVED,
    WORKER_REJECTED,
    WORKER_COUNTERED,
    WORKER_PROPOSED,
    CUSTOMER_ACCEPTED,
    CUSTOMER_REJECTED,
}
