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

## Navigation & Routing
- Transitions between screens now feature high-quality fade and slide animations.
- Role-specific navigation handling is centralized in `AppNavGraph.kt`.
- `NavRoutes` now includes dedicated endpoints for Customer Bookings and Profile management.

## State Management
- All new screens utilize a consistent `UiState` pattern for loading, success, and error handling.
- `ProfileViewModel` now manages both avatar uploads and profile metadata updates in a single reactive flow.
