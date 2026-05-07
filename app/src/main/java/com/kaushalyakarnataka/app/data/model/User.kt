package com.kaushalyakarnataka.app.data.model

import com.google.firebase.Timestamp

/**
 * Represents a user of the Kaushalya-Karnataka app.
 * Both customers and workers share this base model.
 * Role determines which screen flow they see.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val location: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
)

/**
 * User role determines which navigation flow is shown after login.
 * CUSTOMER → CustomerHome, WORKER → WorkerDashboard
 */
enum class UserRole {
    CUSTOMER,
    WORKER,
}
