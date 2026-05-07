package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.BOOKING_UPDATE,
    val bookingId: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class NotificationType {
    BOOKING_REQUEST,
    BOOKING_CONFIRMED,
    BOOKING_DECLINED,
    BOOKING_COMPLETED,
    NEW_REVIEW,
    BOOKING_UPDATE
}
