package com.kaushalyakarnataka.app.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.UserRole
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.screens.auth.*
import com.kaushalyakarnataka.app.ui.screens.common.LoadingScreen
import com.kaushalyakarnataka.app.ui.screens.customer.*
import com.kaushalyakarnataka.app.ui.screens.worker.*
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AuthViewModel

// Smooth enter/exit transitions
fun fadeSlideIn() = fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 }
fun fadeSlideOut() = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 10 }
fun fadeSlideInReverse() = fadeIn(tween(250)) + slideInHorizontally(tween(250)) { -it / 10 }
fun fadeSlideOutReverse() = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 10 }

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.currentUserRole.collectAsState()

    val startDestination = when {
        authState is UiState.Loading -> NavRoutes.WELCOME
        authState is UiState.Success && userRole == UserRole.WORKER -> NavRoutes.WORKER_DASHBOARD
        authState is UiState.Success && userRole == UserRole.CUSTOMER -> NavRoutes.HOME
        else -> NavRoutes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeSlideIn() },
        exitTransition = { fadeSlideOut() },
        popEnterTransition = { fadeSlideInReverse() },
        popExitTransition = { fadeSlideOutReverse() }
    ) {

        // ─── Auth ────────────────────────────────────────────────
        composable(NavRoutes.WELCOME) {
            WelcomeScreen(onGetStartedClick = { navController.navigate(NavRoutes.ROLE_SELECTION) })
        }

        composable(NavRoutes.ROLE_SELECTION) {
            var selectedRole by remember { mutableStateOf<UserRole?>(null) }
            RoleSelectionScreen(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it },
                onContinueClick = {
                    selectedRole?.let { role ->
                        authViewModel.selectRole(role)
                        navController.navigate("auth/${role.name}")
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.AUTH,
            arguments = listOf(navArgument(NavRoutes.Args.ROLE) { type = NavType.StringType })
        ) { back ->
            val role = UserRole.valueOf(back.arguments?.getString(NavRoutes.Args.ROLE) ?: UserRole.CUSTOMER.name)
            AuthScreen(
                role = role,
                onAuthSuccess = {
                    val dest = if (role == UserRole.WORKER) NavRoutes.WORKER_DASHBOARD else NavRoutes.HOME
                    navController.navigate(dest) { popUpTo(NavRoutes.WELCOME) { inclusive = true } }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ─── Customer ────────────────────────────────────────────
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToSearch = { q ->
                    handleCustomerNav(navController, NavDestination.SEARCH)
                },
                onNavigateToCategory = { cat ->
                    navController.navigate("search?query=&category=${cat.name}")
                },
                onNavigateToWorkerProfile = { navController.navigate("worker_profile/$it") },
                onNavigateBottomBar = { handleCustomerNav(navController, it) }
            )
        }

        composable(
            route = NavRoutes.SEARCH,
            arguments = listOf(
                navArgument(NavRoutes.Args.QUERY) { type = NavType.StringType; defaultValue = "" },
                navArgument(NavRoutes.Args.CATEGORY) { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val query = back.arguments?.getString(NavRoutes.Args.QUERY)
            val cat = back.arguments?.getString(NavRoutes.Args.CATEGORY)?.let {
                if (it.isBlank()) null else runCatching { ServiceCategory.valueOf(it) }.getOrNull()
            }
            SearchScreen(
                initialQuery = query,
                initialCategory = cat,
                onNavigateToWorkerProfile = { navController.navigate("worker_profile/$it") },
                onNavigateBottomBar = { handleCustomerNav(navController, it) }
            )
        }

        composable(
            route = NavRoutes.WORKER_PROFILE,
            arguments = listOf(navArgument(NavRoutes.Args.WORKER_ID) { type = NavType.StringType })
        ) { back ->
            val wid = back.arguments?.getString(NavRoutes.Args.WORKER_ID) ?: return@composable
            WorkerProfileScreen(
                workerId = wid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHire = { navController.navigate("hire_request/$it") },
                onNavigateToReviews = { navController.navigate("reviews/$it") }
            )
        }

        composable(
            route = NavRoutes.HIRE_REQUEST,
            arguments = listOf(navArgument(NavRoutes.Args.WORKER_ID) { type = NavType.StringType })
        ) { back ->
            val wid = back.arguments?.getString(NavRoutes.Args.WORKER_ID) ?: return@composable
            HireRequestScreen(
                workerId = wid,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.navigate(NavRoutes.HOME) { popUpTo(NavRoutes.HOME) { inclusive = true } }
                }
            )
        }

        composable(
            route = NavRoutes.REVIEWS,
            arguments = listOf(navArgument(NavRoutes.Args.WORKER_ID) { type = NavType.StringType })
        ) { back ->
            val wid = back.arguments?.getString(NavRoutes.Args.WORKER_ID) ?: return@composable
            ReviewsScreen(workerId = wid, onNavigateBack = { navController.popBackStack() })
        }

        // ─── Customer Tabs ────────────────────────────────────────
        composable(NavRoutes.CUSTOMER_BOOKINGS) {
            CustomerBookingsScreen(
                onNavigateBottomBar = { handleCustomerNav(navController, it) },
                onNavigateToWorkerProfile = { navController.navigate("worker_profile/$it") }
            )
        }

        composable(NavRoutes.CUSTOMER_PROFILE) {
            CustomerProfileScreen(
                onNavigateBottomBar = { handleCustomerNav(navController, it) },
                onLogout = {
                    navController.navigate(NavRoutes.WELCOME) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ─── Worker ───────────────────────────────────────────────
        composable(NavRoutes.WORKER_DASHBOARD) {
            WorkerDashboardScreen(
                onNavigateToAddService = { navController.navigate(NavRoutes.ADD_SERVICE) },
                onNavigateToPortfolio = { navController.navigate(NavRoutes.PORTFOLIO) },
                onNavigateBottomBar = { handleWorkerNav(navController, it) }
            )
        }

        composable(NavRoutes.ADD_SERVICE) {
            AddServiceScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.PORTFOLIO) {
            PortfolioScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.WORKER_PROFILE_TAB) {
            WorkerSelfProfileScreen(
                onNavigateBottomBar = { handleWorkerNav(navController, it) },
                onLogout = {
                    navController.navigate(NavRoutes.WELCOME) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}

private fun handleCustomerNav(navController: NavHostController, dest: NavDestination) {
    val route = when (dest) {
        NavDestination.HOME -> NavRoutes.HOME
        NavDestination.SEARCH -> searchRoute()
        NavDestination.BOOKINGS -> NavRoutes.CUSTOMER_BOOKINGS
        NavDestination.PROFILE -> NavRoutes.CUSTOMER_PROFILE
    }
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun handleWorkerNav(navController: NavHostController, dest: NavDestination) {
    val route = when (dest) {
        NavDestination.HOME -> NavRoutes.WORKER_DASHBOARD
        NavDestination.SEARCH -> NavRoutes.WORKER_DASHBOARD
        NavDestination.BOOKINGS -> NavRoutes.WORKER_DASHBOARD
        NavDestination.PROFILE -> NavRoutes.WORKER_PROFILE_TAB
    }
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun searchRoute(query: String = "", category: String = "") =
    "search?query=${Uri.encode(query)}&category=${Uri.encode(category)}"
