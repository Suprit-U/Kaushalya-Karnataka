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

## Phase 14: UI Polish & Premium Design System

- [x] `ui/components/animations/AnimationUtils.kt` — `AnimatedListItem`, `AnimatedCounter`, `PulseDot`, `CrossfadeContent`.
- [x] `ui/components/animations/ShimmerEffect.kt` — `ShimmerBox`, `ShimmerCard`, `ShimmerCircle`, `ShimmerTextLine`.
- [x] `ui/components/common/PremiumCards.kt` — `GradientCard`, `ElevatedSurfaceCard`, `StatCard`, `PremiumButton`, `PremiumOutlinedButton`, `EmptyStateView`, `StatusChip`, `GlassSurface`.
- [x] `ui/components/WorkerCard.kt` — Redesigned with gradient accent, press scale, premium typography, and shadow.
- [x] `ui/components/KaushalyaBottomNav.kt` — Floating pill-style nav with animated active indicator, icon scaling, and label expand/collapse.
- [x] `ui/screens/customer/HomeScreen.kt` — Hero gradient with wave animation, glass search bar, category shadows, `TopWorkerCard` gradient CTA, accent `SectionHeader`.
- [x] `ui/screens/worker/WorkerDashboardScreen.kt` — Glass header with pulse badge, `PremiumStatCard` with gradient, `EnhancedJobCard` with shadow and gradient actions.
- [x] `ui/screens/customer/WorkerProfileScreen.kt` — 4-stop gradient hero, glass back button, accent `ProfileSection`, premium `ServicePricingRow`/`ReviewPreviewCard`, gradient bottom CTA.
- [x] `ui/screens/common/NotificationsScreen.kt` — Unread accent border, larger icon containers, improved typography, dot indicator with border.

## Phase 15: Dark Mode, Navigation & Bookings Premium Polish

- [x] `ui/theme/Theme.kt` — Overhauled `DarkColorScheme` with AMOLED-friendly backgrounds, rich tonal surfaces (`DarkSurface`, `DarkSurfaceContainer`, etc.), and full Material 3 dark token coverage.
- [x] `ui/theme/Color.kt` — Added comprehensive dark mode color tokens: `DarkBackground`, `DarkSurface`, `DarkSurfaceVariant`, `DarkSurfaceContainer` family, `DarkOutline`, `DarkInversePrimary`.
- [x] `ui/theme/Typography.kt` — Added `Manrope` Google Font. Upgraded hierarchy: Manrope for titles/labels, Poppins for display/headlines, Inter for body. Larger sizes, better line-heights, refined letter-spacing.
- [x] `ui/screens/customer/SearchScreen.kt` — Fixed navbar visibility issue by integrating `KaushalyaBottomNav` and `onNavigateBottomBar`. Search/Explore is now a proper persistent tab.
- [x] `navigation/AppNavGraph.kt` — Search tab navigation uses `handleCustomerNav` with `saveState`/`restoreState` for seamless tab switching from HomeScreen.
- [x] `ui/components/WorkerBottomNav.kt` — Complete pill-style redesign matching customer nav: floating rounded surface, spring pill expand, `AnimatedVisibility` labels, press-scale, shadow.
- [x] `ui/screens/customer/CustomerBookingsScreen.kt` — Premium `CustomerBookingCard` redesign: status icon containers, gradient amount badges, `StatusPanel` component, premium `InfoChip`, rounded buttons. Empty state upgraded with illustration container.
- [x] `ui/screens/worker/WorkerDashboardScreen.kt` — BookingsTab empty state and `InfoChip` upgraded with premium icon containers.
- [x] `ui/components/animations/AnimationUtils.kt` — Added `ElasticPressEffect` (spring-based tactile press) and `ShimmerSweep` (smooth shimmer animation).

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
- [x] Premium UI polish pass — animations, shadows, gradients, typography, shimmer loaders, pill navigation.

## Phase 3: Premium Nav, Auth & Screen Overhaul

- [x] `ui/components/KaushalyaBottomNav.kt` — Redesigned with true animated sliding pill indicator (`onGloballyPositioned` measurement + spring physics), gradient active pill, elastic press animations, enhanced 12dp shadow.
- [x] `ui/screens/auth/WelcomeScreen.kt` — Complete redesign with radial gradient backgrounds, 3 floating orbs, pulsing logo in glass container, split headline typography, premium gradient CTA.
- [x] `ui/screens/auth/RoleSelectionScreen.kt` — Gradient header, premium role cards with icon containers, spring selection scale, animated border colors, checkmark indicator, gradient continue button.
- [x] `ui/screens/auth/AuthScreen.kt` — Animated header title transitions, glass segmented control, `PremiumTextField` with focus-tracking icon containers, animated border/label colors, premium error display, gradient action button with loading scale.
- [x] `ui/screens/customer/HomeScreen.kt` — Auto-scroll promo banners (3s interval), floating decorative orbs in hero header.
- [x] `ui/components/InputField.kt` — `SearchBarField` premium redesign: shadow, icon container, animated border, refined colors.
- [x] `ui/components/ChipRow.kt` — Replaced `FilterChip` with custom `PremiumChip`: animated color transitions, selected shadow, bold typography.
- [x] `ui/components/SearchResultCard.kt` — Surface + shadow, press-scale interaction, `VerifiedBadge`, rating badge container, price tag container.
- [x] `ui/screens/customer/CustomerProfileScreen.kt` — Gradient header with transparent top bar, avatar ring, icon containers for all menu items, gradient logout button.
- [x] `ui/screens/worker/WorkerDashboardScreen.kt` — Floating decorative orbs in dashboard header.
- [x] `ui/screens/worker/WorkerSelfProfileScreen.kt` — Floating orb in hero, settings icon containers, gradient logout button.
- [x] Build verified: `clean assembleDebug lintRelease` passes successfully.

## Phase 4: Navbar Fix, Profile & Bookings Polish

- [x] `ui/components/KaushalyaBottomNav.kt` — **Alignment fix**: Replaced complex `onGloballyPositioned` sliding indicator with simple weight-based `animateDpAsState` pill width. Perfect centering via `Arrangement.SpaceEvenly` + `Modifier.weight(1f)`. Consistent press scale, icon color tween, and spring pill width animations.
- [x] `ui/screens/customer/HomeScreen.kt` — Greeting changed "Namaskara" → "Namaste 👋". Single shared `rememberInfiniteTransition` for all hero animations (eliminates nested transitions). Orbs use `LinearEasing` with slower 6000ms/7000ms durations for organic motion. Wave uses `FastOutSlowInEasing` with 4000ms/3px range for smooth floating.
- [x] `ui/screens/customer/CustomerProfileScreen.kt` — Full redesign: transparent top bar over gradient header, floating orb, 112dp glass avatar ring, white camera button with shadow, role badge with border stroke, quick stats row (`CustomerStatCard` x3 with overlapping negative offset), section cards with shadow + border stroke, 36dp icon containers for all menu items, gradient logout button.
- [x] `ui/screens/worker/WorkerSelfProfileScreen.kt` — Full redesign: transparent top bar over gradient header, 108dp glass avatar ring, larger verified badge (30dp), improved typography hierarchy (`headlineSmall` + `ExtraBold` for name), premium chips with `BorderStroke` glass effect, `WorkerStatCard` x3 stats strip with overlapping offset, `SectionBlock` cards with shadow + border, 36dp settings icon containers, gradient logout button.
- [x] `ui/screens/worker/WorkerDashboardScreen.kt` — `EnhancedJobCard` redesigned: status-colored initial avatar placeholder, `MutableInteractionSource` press tracking, `BorderStroke` status-tinted card border, premium `PrimaryTint` amount badge, renamed `JobInfoChip`. `BookingsTab` redesigned: tab count badges with dynamic color, improved empty state (100dp container with border), keyed list items, increased spacing.
- [x] Build verified: `clean assembleDebug lintRelease` passes successfully.

## Phase 5: Theme System, Dark Mode Persistence, Navbar Premium Animations & Global Consistency

- [x] `app/ui/theme/Theme.kt` — **DataStore persistence**: `ThemeState` now reads from/writes to `ThemePreferenceManager` (DataStore). `KaushalyaTheme` accepts `preferenceManager` and syncs state via `collectAsState`. Dark mode preference survives app restarts.
- [x] `app/data/local/ThemePreferenceManager.kt` — New DataStore helper using `booleanPreferencesKey("dark_mode")` for persisting theme state.
- [x] `app/di/AppModule.kt` — Added `@Provides` for `ThemePreferenceManager` in companion object.
- [x] `app/MainActivity.kt` — Injects `ThemePreferenceManager` and passes it to `KaushalyaTheme`.
- [x] `app/ui/theme/Color.kt` — Dark mode tokens redesigned: `DarkBackground` (`0xFF02040A`), `DarkSurface` (`0xFF0B1221`), `DarkSurfaceVariant` (`0xFF141D2E`) — richer navy surfaces with micro blue tint, avoiding flat gray. Added `DarkPrimaryTint` and `DarkCardBorder`.
- [x] `ui/components/KaushalyaBottomNav.kt` — **Premium animations**: Gradient active pill (`Primary → PrimaryLight`), icon scale animation (`1.0f → 1.15f`), smoother spring physics (`stiffness=320f, dampingRatio=0.75f`), `expandHorizontally` with spring for label animation.
- [x] `ui/components/WorkerBottomNav.kt` — **Matching premium animations**: Same gradient pill, icon scale, spring physics as customer navbar for design parity.
- [x] `ui/screens/worker/WorkerSelfProfileScreen.kt` — **Proper dark mode switch**: Replaced `SettingsRow` with new `SettingsSwitchRow` composable using Material3 `Switch` with custom `SwitchDefaults.colors`. Wraps `themeState.toggle()` in `rememberCoroutineScope().launch`.
- [x] `ui/screens/customer/CustomerProfileScreen.kt` / `HomeScreen.kt` — Fixed `themeState.toggle()` suspend calls by wrapping in `rememberCoroutineScope().launch`.
- [x] `ui/screens/worker/WorkerDashboardScreen.kt` — **Timeline booking list**: Each `EnhancedJobCard` now wrapped in a timeline row with vertical status-colored connector lines and status dots between cards, creating a journey/progress feeling.
- [x] `ui/screens/auth/WelcomeScreen.kt` — **Logo branding**: Replaced Handshake icon with `logo.png` (painterResource) in glass container with pulse animation.
- [x] `ui/screens/auth/AuthScreen.kt` — **Logo branding**: Added `logo.png` (36dp) to gradient header next to back button.
- [x] `app/src/main/res/drawable/logo.png` — Copied project root logo into drawable resources.
- [x] Build verified: `clean assembleDebug lintRelease` passes successfully.

## Phase 6: AI Review Summary (Customer Side)

- [x] `app/build.gradle.kts` — **Secure API key storage**: Reads `OPENROUTER_API_KEY` from `local.properties` and injects into `BuildConfig` via `buildConfigField`. Never hardcoded in source.
- [x] `app/data/model/openrouter/OpenRouterModels.kt` — Data models for OpenRouter API: `OpenRouterRequest`, `Message`, `OpenRouterResponse`, `Choice`, `MessageContent`, `OpenRouterError`.
- [x] `app/data/remote/OpenRouterService.kt` — Ktor-based HTTP client for OpenRouter API. Calls `openai/gpt-oss-120b` with system prompt enforcing 1-3 line summaries and no hallucinations. Uses `BuildConfig.OPENROUTER_API_KEY` in Authorization header.
- [x] `app/data/repository/AiSummaryRepository.kt` — Repository with DataStore caching. 24-hour cache TTL via `longPreferencesKey`. Checks cache before API call. Falls back to API only if cache expired or missing. Minimum 2 reviews required.
- [x] `app/viewmodel/AiSummaryViewModel.kt` — Hilt ViewModel managing `UiState<String>` for summary. `generateSummary()` triggers repository, `retry()` clears cache and regenerates.
- [x] `app/ui/components/AiReviewSummaryCard.kt` — Premium summary card with gradient border, AI sparkle icon in glass container. Handles all states: shimmer loading (`ShimmerLine`), error with retry, success with fade animation. Supports both light and dark mode via `MaterialTheme` tokens.
- [x] `app/ui/screens/customer/WorkerProfileScreen.kt` — Integrated `AiReviewSummaryCard` into Reviews section below rating summary. `LaunchedEffect` triggers generation when reviews load. Injected `AiSummaryViewModel` alongside existing `WorkerProfileViewModel`.
- [x] `app/di/AppModule.kt` — Added `@Binds` for `AiSummaryRepository`/`AiSummaryRepositoryImpl`.
- [x] Build verified: `clean assembleDebug lintRelease` passes successfully.

- [ ] Automated unit and UI tests.
- [ ] Full localization pass.
- [ ] Geo-location based worker filtering.
