package com.kaushalyakarnataka.app.data.model

/**
 * A service offered by a worker.
 * Workers create these via AddServiceScreen.
 * Displayed in the "Services & Pricing" section of WorkerProfileScreen.
 */
data class Service(
    val id: String = "",
    val workerId: String = "",
    val name: String = "",               // e.g. "Wiring & Rewiring"
    val category: ServiceCategory = ServiceCategory.ELECTRICIAN,
    val description: String = "",
    val startingPrice: Int = 0,          // In rupees
    val pricingType: PricingType = PricingType.HOURLY,
    val estimatedDuration: ServiceDuration = ServiceDuration.ONE_HOUR,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
)

/**
 * How the worker charges for their service.
 */
enum class PricingType(val displayLabel: String) {
    HOURLY("Per Hour"),
    FIXED("Fixed Price"),
    STARTING_AT("Starting at"),
}

/**
 * Estimated time to complete the service.
 * Shown in AddServiceScreen duration selector and on worker profile.
 */
enum class ServiceDuration(val displayLabel: String) {
    ONE_HOUR("1 hr"),
    TWO_TO_THREE_HOURS("2–3 hrs"),
    FULL_DAY("Full day"),
}

/**
 * Portfolio item — a photo uploaded by the worker showing their work.
 */
data class PortfolioItem(
    val id: String = "",
    val workerId: String = "",
    val photoUrl: String = "",
    val caption: String = "",
    val serviceCategory: ServiceCategory = ServiceCategory.OTHER,
)

/**
 * Aggregated stats shown on PortfolioScreen.
 */
data class PortfolioStats(
    val projectCount: Int = 0,
    val photoCount: Int = 0,
    val averageRating: Double = 0.0,
)

/**
 * Worker earnings summary shown on the dashboard.
 */
data class EarningsData(
    val thisMonthTotal: Int = 0,        // In rupees
    val lastMonthTotal: Int = 0,
    val percentageChange: Int = 0,      // vs last month
    val completedJobs: Int = 0,
    val pendingJobs: Int = 0,
    val averageRating: Double = 0.0,
)
