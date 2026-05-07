package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

/**
 * Represents a service booking between a customer and a worker.
 * Created when customer taps "Confirm Booking Request" on HireRequestScreen.
 */
data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val service: String = "",
    val scheduledDate: Timestamp = Timestamp.now(),
    val timeSlot: String = "",           // e.g. "9:00 AM"
    val address: String = "",
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val estimatedCostMin: Int = 0,       // e.g. 450
    val estimatedCostMax: Int = 0,       // e.g. 900
    val bookingCode: String = "",        // e.g. "#KK-2024-0106"
    val couponCode: String = "",
    val discountAmount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
)

/**
 * Booking status lifecycle:
 *   PENDING → Worker sees "NEW" badge, can accept or decline
 *   CONFIRMED → Worker accepted, customer gets notification
 *   IN_PROGRESS → Worker checked in at location
 *   COMPLETED → Job done, customer can review
 *   CANCELLED → Either party cancelled
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
}
