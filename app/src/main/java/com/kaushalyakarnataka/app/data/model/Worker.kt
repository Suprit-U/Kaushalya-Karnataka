package com.kaushalyakarnataka.app.data.model

/**
 * Worker profile — extends the base user with service-specific fields.
 * Populated from the /workers/{uid} Firestore collection.
 */
data class Worker(
    val uid: String = "",
    val name: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val role: String = "",               // Display role e.g. "Master Electrician"
    val bio: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val experienceYears: Int = 0,
    val successRate: Int = 0,            // Percentage, e.g. 98
    val pricePerHour: Int = 0,           // In rupees
    val distanceKm: Double = 0.0,
    val isAvailable: Boolean = true,
    val isVerified: Boolean = false,
    val isGovernmentCertified: Boolean = false,
    val tags: List<String> = emptyList(),
    val portfolioUrls: List<String> = emptyList(),
    val avatarUrl: String = "",
    val phone: String = "",
    val location: String = "",
)

/**
 * Service categories shown in the home screen category grid.
 * Maps to cat-icon colors defined in the HTML prototype.
 */
enum class ServiceCategory(val displayName: String, val emoji: String) {
    ELECTRICIAN("Electrician", "⚡"),
    PLUMBER("Plumber", "🔧"),
    CARPENTER("Carpenter", "🪚"),
    PAINTER("Painter", "🎨"),
    CLEANER("Cleaner", "🧹"),
    AC_TECH("AC Tech", "❄️"),
    GARDENER("Gardener", "🌿"),
    MECHANIC("Mechanic", "🧰"),
    OTHER("Other", "🔨"),
}
