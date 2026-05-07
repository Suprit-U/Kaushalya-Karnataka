package com.kaushalyakarnataka.app.navigation

/**
 * Defines all navigation route strings for the app.
 * Arguments are appended as path segments: e.g., "worker_profile/{workerId}"
 */
object NavRoutes {

    // Auth Flow
    const val WELCOME = "welcome"
    const val ROLE_SELECTION = "role_selection"
    const val AUTH = "auth/{role}"

    // Customer Flow
    const val HOME = "home"
    const val SEARCH = "search?query={query}&category={category}"
    const val WORKER_PROFILE = "worker_profile/{workerId}"
    const val HIRE_REQUEST = "hire_request/{workerId}"
    const val REVIEWS = "reviews/{workerId}"
    const val CUSTOMER_BOOKINGS = "customer_bookings"
    const val WORKER_BOOKINGS = "worker_bookings"
    const val CUSTOMER_PROFILE = "customer_profile"

    // Worker Flow
    const val WORKER_DASHBOARD = "worker_dashboard"
    const val ADD_SERVICE = "add_service"
    const val PORTFOLIO = "portfolio"
    const val WORKER_PROFILE_TAB = "worker_profile_tab"
    const val EDIT_PROFILE = "edit_profile"

    // Argument Keys
    object Args {
        const val ROLE = "role"
        const val WORKER_ID = "workerId"
        const val QUERY = "query"
        const val CATEGORY = "category"
    }
}
