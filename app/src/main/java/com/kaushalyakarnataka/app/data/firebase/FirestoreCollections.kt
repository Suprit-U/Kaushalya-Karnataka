package com.kaushalyakarnataka.app.data.firebase

/**
 * Centralized Firestore collection and field name constants.
 * Use these instead of raw strings to avoid typos and ease refactoring.
 *
 * Firestore structure:
 *   /users/{uid}           → User profiles
 *   /workers/{uid}         → Worker-specific profiles (subset of users)
 *   /bookings/{bookingId}  → Service bookings
 *   /reviews/{reviewId}    → Customer reviews
 *   /services/{serviceId}  → Services offered by workers
 */
object FirestoreCollections {

    // --- Collection names ---
    const val USERS     = "users"
    const val WORKERS   = "workers"
    const val BOOKINGS  = "bookings"
    const val REVIEWS   = "reviews"
    const val SERVICES  = "services"
    const val NOTIFICATIONS = "notifications"

    // --- Common field names ---
    object Fields {
        const val UID           = "uid"
        const val NAME          = "name"
        const val PHONE         = "phone"
        const val EMAIL         = "email"
        const val ROLE          = "role"
        const val CREATED_AT    = "createdAt"
        const val IS_VERIFIED   = "isVerified"
        const val AVATAR_URL    = "avatarUrl"
        const val LOCATION      = "location"

        // Worker fields
        const val CATEGORY      = "category"
        const val RATING        = "rating"
        const val REVIEW_COUNT  = "reviewCount"
        const val IS_AVAILABLE  = "isAvailable"
        const val BASE_LABOUR_CHARGE = "baseLabourCharge"
        const val PRICE_PER_HOUR = "pricePerHour"
        const val EXPERIENCE_YEARS = "experienceYears"
        const val SUCCESS_RATE  = "successRate"

        // Booking fields
        const val CUSTOMER_ID   = "customerId"
        const val WORKER_ID     = "workerId"
        const val STATUS        = "status"
        const val SCHEDULED_DATE = "scheduledDate"
        const val TIME_SLOT     = "timeSlot"
        const val ADDRESS       = "address"

        // Review fields
        const val RATING_VALUE  = "rating"
        const val COMMENT       = "comment"
        const val HELPFUL_COUNT = "helpfulCount"
        const val SERVICE_TYPE  = "serviceType"

        // Service fields
        const val SERVICE_NAME  = "name"
        const val STARTING_PRICE = "startingPrice"
        const val PRICING_TYPE  = "pricingType"
        const val DESCRIPTION   = "description"
        const val IS_ACTIVE     = "isActive"

        // Notification fields
        const val USER_ID     = "userId"
        const val IS_READ     = "isRead"
        const val TITLE       = "title"
        const val MESSAGE     = "message"
    }
}

/**
 * Firebase Storage path helpers.
 */
object StoragePaths {
    fun avatarPath(uid: String) = "avatars/$uid/profile.jpg"
    fun portfolioPath(uid: String, fileName: String) = "portfolio/$uid/$fileName"
    fun reviewPhotoPath(bookingId: String, fileName: String) = "reviews/$bookingId/$fileName"
}
