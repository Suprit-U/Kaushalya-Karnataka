# Knowledge Base

## Recent Architecture & UI Fixes
- **BookingRepository**: Fixed a compilation crash caused by unsafe destructuring of nullable values in the booking status logic. Added explicit null checks.
- **Portfolio Management**: Implemented missing `portfolioItems` StateFlow and `deletePortfolioItem` function in `PortfolioViewModel`.
- **Type Safety**: Resolved type inference errors in `PortfolioScreen.kt` by providing explicit type arguments to `UiState.Success`.
- **Room Consolidation**: Replaced multiple Room DAO and Entity files with a single, optimized [LocalDatabase.kt](file:///d:/Dev-Tools/Android-Studio/Projects/Kaushalya-Karnataka/app/src/main/java/com/kaushalyakarnataka/app/data/local/LocalDatabase.kt).
- **Consolidated DI**: Integrated database provisioning into `DatabaseModule.kt` for cleaner dependency management.
- **Advanced Navigation**: Replaced the standard `BottomNavBar` with `KaushalyaBottomNav`, featuring custom animations and role-based destination management.
- **Premium UI Overhaul**: Successfully merged the `New` folder implementation, providing a more refined and feature-complete user experience.
- **Worker Self-Profile**: Refactored the self-profile logic to support a consistent navigation signature across all user roles.

## Notification System Fixes
- **Root Cause**: `NotificationViewModel.load()` launched two concurrent coroutines that both wrote to `_notifications`, causing a race condition where the one-time fetch overwrote real-time observer data.
- **Fix**: Removed concurrent one-time fetch from `load()`. Rely purely on `observeNotifications()` real-time stream. `markAllRead()` now just calls repo and lets the listener update state automatically.
- **Query Improvement**: Added `orderBy("createdAt", DESCENDING)` and `limit(50)` to `observeNotifications` for consistent ordering.
- **Constants**: Added `NOTIFICATIONS` collection and `USER_ID`, `IS_READ`, `TITLE`, `MESSAGE` field constants to `FirestoreCollections`.

## Portfolio Gallery Viewer
- **Implementation**: Added `PortfolioGalleryDialog` in `WorkerProfileScreen` with `HorizontalPager`, pinch-to-zoom via `detectTransformGestures`, double-tap zoom toggle, and caption overlay.
- **UX**: Full-screen black overlay, close button, page counter, tap-to-dismiss, smooth transitions.

## Search & Category Filtering Fixes
- **Root Cause**: `WorkerRepository.searchWorkers()` used `whereEqualTo(CATEGORY) + orderBy(RATING)` when a category was selected, requiring a Firestore composite index that may not exist. On failure, it silently returned `Success(emptyList())`.
- **Fix**: Removed `orderBy` from the category query branch; results are sorted in-memory with `.sortedByDescending { it.rating }`. Now returns `UiState.Error` on genuine exceptions.
- **Category Consistency**: `HomeCategoryGrid` and `CategoryChipRow` both use `ServiceCategory.entries.filter { it != ServiceCategory.OTHER }` with a dynamic `when` mapping for icons/colors.

## Firestore Indexing & Notification Reliability
- **Issue**: `FAILED_PRECONDITION: The query requires an index` thrown by notification queries using `whereEqualTo(userId) + orderBy(createdAt DESC)` and `whereEqualTo(userId) + whereEqualTo(isRead)`.
- **Root Fix**: Restructured ALL notification queries in `NotificationRepository` to use **only single-field equality filters** (`whereEqualTo(userId)`). Firestore composite indexes are no longer required for the app to function.
  - `observeNotifications` / `getNotifications`: query by `userId` only, sort by `createdAt` descending client-side, take top 50.
  - `markAllRead`: query by `userId` only, filter `isRead==false` client-side, batch-update.
  - `getUnreadCount`: query by `userId` only, count unread client-side.
- **Production**: `firestore.indexes.json` still defines optional composite indexes for performance at scale. Deploy with `firebase deploy --only firestore:indexes` when ready.
- **Friendly Errors**: `NotificationRepository` maps raw Firebase exception messages to user-friendly strings. `NotificationsScreen`, `HomeNotificationsDialog`, and `DashboardNotificationsDialog` display clean error UI with icons and retry — never raw exceptions.
- **Logging**: All notification operations log with `NotificationRepository` tag: query start, success counts, snapshot updates, batch commits, and parse failures.

## Worker Bio Editing
- **Implementation**: Added dedicated "Edit Bio" button in `WorkerSelfProfileScreen` next to the "About Me" section.
- **Dialog**: `BioEditDialog` with character counter (500 max), validation, Material 3 styling, and placeholder text.
- **Backend**: `WorkerProfileViewModel.updateBio(bio)` calls `WorkerRepository.updateWorkerProfile()` with a single-field map, then refreshes the profile stream. The customer-facing `WorkerProfileScreen` already displays the bio under "About Me".
- **Feedback**: SnackbarHost in `WorkerSelfProfileScreen` shows "Profile updated successfully" or error message on save.

## Navigation & Routing
- Transitions between screens now feature high-quality fade and slide animations.
- Role-specific navigation handling is centralized in `AppNavGraph.kt`.
- `NavRoutes` now includes dedicated endpoints for Customer Bookings and Profile management.

## State Management
- All new screens utilize a consistent `UiState` pattern for loading, success, and error handling.
- `ProfileViewModel` now manages both avatar uploads and profile metadata updates in a single reactive flow.
