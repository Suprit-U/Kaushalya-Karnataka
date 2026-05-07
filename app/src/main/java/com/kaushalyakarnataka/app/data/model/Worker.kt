package com.kaushalyakarnataka.app.data.model

/**
 * Worker profile — extends the base user with service-specific fields.
 */
data class Worker(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val role: String = "",
    val bio: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val baseLabourCharge: Int = 0,
    val pricePerHour: Int = 0,
    val distanceKm: Double = 0.0,
    val isAvailable: Boolean = true,
    val isVerified: Boolean = false,
    val isGovernmentCertified: Boolean = false,
    val tags: List<String> = emptyList(),
    val portfolioUrls: List<String> = emptyList(),
    val avatarUrl: String = "",
    val location: String = "",
    val experienceYears: Int = 0,
    val successRate: Int = 100,
) {
    val displayBaseCharge: Int
        get() = if (baseLabourCharge > 0) baseLabourCharge else pricePerHour
}

/**
 * Service categories shown in the home screen category grid.
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
