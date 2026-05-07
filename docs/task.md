# Kaushalya-Karnataka Build Task List

Last updated: 2026-05-06

## Build Verification

- [x] Full Gradle build passes with `.\gradlew.bat build`.
- [x] Kotlin compile errors fixed.
- [x] Android lint errors fixed.
- [x] Release build has a valid `app/proguard-rules.pro`.
- [x] No active TODO/FIXME/stub/incomplete markers remain in authored project files.

## Phase 1: Project Setup and Gradle

- [x] `gradle/libs.versions.toml`
- [x] `settings.gradle.kts`
- [x] `build.gradle.kts`
- [x] `app/build.gradle.kts`
- [x] `gradle.properties`
- [x] `app/proguard-rules.pro`

## Phase 2: App Entry and Manifest

- [x] `app/src/main/AndroidManifest.xml`
- [x] `KaushalyaApp.kt`
- [x] `MainActivity.kt`
- [x] Camera hardware declared optional.
- [x] GPS hardware declared optional.
- [x] Android 14 selected photos permission declared.

## Phase 3: Theme Layer

- [x] `ui/theme/Color.kt`
- [x] `ui/theme/Typography.kt`
- [x] `ui/theme/Dimens.kt`
- [x] `ui/theme/Shape.kt`
- [x] `ui/theme/Theme.kt`

## Phase 4: Data Models

- [x] `data/model/User.kt`
- [x] `data/model/Worker.kt`
- [x] `data/model/Booking.kt`
- [x] `data/model/Review.kt`
- [x] `data/model/Service.kt`
- [x] `data/model/Notification.kt`
- [x] Pricing, duration, booking status, and rating stats models aligned with callers.

## Phase 5: Firebase, Supabase, and DI

- [x] `data/firebase/FirebaseModule.kt`
- [x] `data/firebase/FirestoreCollections.kt`
- [x] `di/AppModule.kt`
- [x] `di/SupabaseModule.kt`
- [x] Firebase repository bindings.
- [x] Supabase storage provider.

## Phase 6: Repositories

- [x] `data/repository/AuthRepository.kt`
- [x] `data/repository/WorkerRepository.kt`
- [x] `data/repository/BookingRepository.kt`
- [x] `data/repository/ReviewRepository.kt`
- [x] `data/repository/ServiceRepository.kt`
- [x] `data/repository/StorageRepository.kt`
- [x] `data/repository/NotificationRepository.kt` — Restructured ALL queries to use single-field equality only (`whereEqualTo(userId)`). Client-side sorting and filtering eliminate composite-index requirements. Friendly error mapping. Comprehensive logging for all operations.
- [x] `firestore.indexes.json` — Composite indexes for notifications, workers, reviews, bookings.
- [x] Sample fallback data compiles against current models.

## Phase 7: Utils

- [x] `utils/UiState.kt`
- [x] `utils/DateUtils.kt`
- [x] `utils/CurrencyUtils.kt`
- [x] `utils/Extensions.kt`

## Phase 8: ViewModels

- [x] `viewmodel/AuthViewModel.kt`
- [x] `viewmodel/HomeViewModel.kt`
- [x] `viewmodel/SearchViewModel.kt`
- [x] `viewmodel/WorkerProfileViewModel.kt`
- [x] `viewmodel/HireViewModel.kt`
- [x] `viewmodel/ReviewViewModel.kt`
- [x] `viewmodel/WorkerDashboardViewModel.kt`
- [x] `viewmodel/ServiceViewModel.kt`
- [x] `viewmodel/PortfolioViewModel.kt`
- [x] `viewmodel/NotificationViewModel.kt` — Removed race condition in `load()`, now uses single real-time observer.
- [x] `viewmodel/WorkerProfileViewModel.kt` — Added `updateBio()` for single-field bio updates.

## Phase 9: UI Components

- [x] `ui/components/AppTopBar.kt`
- [x] `ui/components/BottomNavBar.kt`
- [x] `ui/components/WorkerCard.kt`
- [x] `ui/components/SearchResultCard.kt`
- [x] `ui/components/CategoryGrid.kt`
- [x] `ui/components/PromoBanner.kt`
- [x] `ui/components/RatingBar.kt`
- [x] `ui/components/SkeletonLoader.kt`
- [x] `ui/components/EmptyState.kt`
- [x] `ui/components/TimeSlotPicker.kt`
- [x] `ui/components/DatePickerRow.kt`
- [x] `ui/components/ReviewCard.kt`
- [x] `ui/components/RatingBreakdown.kt`
- [x] `ui/components/JobCard.kt`
- [x] `ui/components/QuickActionGrid.kt`
- [x] `ui/components/ChipRow.kt`
- [x] `ui/components/InputField.kt`
- [x] `ui/components/Buttons.kt`
- [x] `ui/components/AvatarComponent.kt`
- [x] `ui/components/BadgeComponent.kt`
- [x] `ui/components/HireSuccessModal.kt`
- [x] `ui/components/PortfolioGrid.kt`
- [x] `ui/components/KaushalyaBottomNav.kt`: Custom animated bottom navigation.
- [x] Exhaustive UI handling for booking statuses and service categories.

## Phase 10: Screens

- [x] `ui/screens/auth/WelcomeScreen.kt`
- [x] `ui/screens/auth/RoleSelectionScreen.kt`
- [x] `ui/screens/auth/AuthScreen.kt`
- [x] `ui/screens/customer/HomeScreen.kt` — Category grid now derives from `ServiceCategory.entries` dynamically.
- [x] `ui/screens/customer/SearchScreen.kt` — Category filtering fixed; no longer requires Firestore composite index.
- [x] `ui/screens/customer/WorkerProfileScreen.kt` — Added `PortfolioGalleryDialog` with zoom, swipe, and captions.
- [x] `ui/screens/customer/HireRequestScreen.kt`
- [x] `ui/screens/customer/CustomerBookingsScreen.kt`
- [x] `ui/screens/customer/CustomerProfileScreen.kt`
- [x] `ui/screens/worker/WorkerDashboardScreen.kt`
- [x] `ui/screens/worker/WorkerSelfProfileScreen.kt` — Added `BioEditDialog` with character counter, validation, Snackbar feedback, and dedicated "Edit Bio" action.
- [x] `ui/screens/worker/AddServiceScreen.kt`
- [x] `ui/screens/worker/PortfolioScreen.kt`
- [x] `ui/screens/common/NotificationsScreen.kt`
- [x] `ui/screens/common/LoadingScreen.kt`
- [x] Worker dashboard quick actions wired to real dashboard sections.

## Phase 11: Navigation

- [x] `navigation/NavRoutes.kt`
- [x] `navigation/AppNavGraph.kt`
- [x] Auth start destination logic.
- [x] Customer navigation flow.
- [x] Worker navigation flow.
- [x] Bottom navigation routes compile and avoid placeholder TODOs.

## Phase 12: Resources

- [x] `res/values/strings.xml`
- [x] `res/values/colors.xml`
- [x] `res/values/themes.xml`
- [ ] Optional future work: `res/font/` font XML configs.
- [ ] Optional future work: move hardcoded Compose strings to XML resources for localization.

## Phase 13: Documentation

- [x] `docs/knowledge_base.md`
- [x] `docs/agent.md`
- [x] `docs/firebase_setup.md`
- [x] `docs/project_structure.md`
- [x] `docs/implementation_plan.md`
- [x] `docs/supabase/setup_guide.md`
- [x] `docs/task.md`

## Future Product Enhancements

- [ ] Production Firestore security rules.
- [ ] BuildConfig/env-based Supabase credentials.
- [x] Dedicated customer bookings screen.
- [x] Dedicated customer profile screen.
- [x] Portfolio gallery viewer with zoom/swipe.
- [x] Search + category filtering without composite index dependency.
- [x] Notification system with real-time badge and list.
- [x] Firestore composite indexes defined and documented.
- [x] Friendly error handling in notification repository and UI (no raw Firebase errors shown to users).
- [x] Worker bio editing with dedicated dialog, character limit, and Snackbar feedback.
- [ ] Automated unit and UI tests.
- [ ] Full localization pass.
- [ ] Geo-location based worker filtering.
