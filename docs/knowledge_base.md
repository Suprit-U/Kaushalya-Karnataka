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

## Phase 3: Premium Customer Nav & Auth Overhaul

### Customer Navbar Redesign
- **Sliding indicator**: `KaushalyaBottomNav.kt` now features a true animated sliding pill background using `onGloballyPositioned` to measure each tab's position and `animateFloatAsState` with spring physics for smooth horizontal movement.
- **Gradient active pill**: The indicator uses a `Brush.horizontalGradient(listOf(Primary, PrimaryLight))` instead of solid color.
- **Elastic animations**: Icon scale animates independently with `spring(stiffness=500f, dampingRatio=0.4f)` for a bouncy feel. Container uses `spring(stiffness=400f, dampingRatio=0.5f)`.
- **Enhanced shadow**: Elevated to 12dp with both `spotColor` and `ambientColor` for richer depth.
- **Unselected press feedback**: Subtle `Primary.copy(0.06f)` background appears when pressing unselected tabs.

### Auth Screens Premium Redesign
- **WelcomeScreen**: Complete redesign with animated radial gradient backgrounds, three floating decorative orbs with independent infinite transitions, pulsing logo in a glass container, split "Kaushalya / Karnataka" headline typography, and a premium gradient CTA button.
- **RoleSelectionScreen**: Gradient header matching auth theme, premium role cards with icon containers (56dp rounded squares), spring-based selection scale animation (`1.02f`), animated border color transitions, checkmark indicator for selected state, and gradient continue button.
- **AuthScreen**: Animated `AnimatedContent` header title transitions between login/signup, glass segmented control with white pill indicator, `PremiumTextField` composable with focus-tracking icon containers (36dp rounded squares), animated border/label/background colors on focus, premium error display with icon container, and gradient action button with loading scale animation.

### Customer Home Improvements
- **Auto-scroll promo banners**: `PromoBannerCarousel` now uses `LazyListState` with `LaunchedEffect` that auto-scrolls every 3 seconds with `animateScrollToItem` for a dynamic, alive feel.
- **Floating hero orbs**: `HomeHeroHeader` gained two floating decorative orbs with independent animation timings for added depth.

### Search/Explore Premium Polish
- **SearchBarField**: Now features a shadow (4dp), search icon in a 36dp rounded container with `Primary.copy(0.08f)` background, animated border color that transitions to `Primary` when query is non-empty, and refined `OutlinedTextFieldDefaults` with cursor and text colors.
- **CategoryChipRow**: Completely replaced `FilterChip` with custom `PremiumChip` using `Surface` with animated color transitions, shadow elevation on selected state, and bold typography.
- **SearchResultCard**: Redesigned with `Surface` + shadow (3dp), press-scale interaction via `MutableInteractionSource`, `VerifiedBadge` support, rating badge in a tinted container, and price tag in `PrimaryTint` container.

### Customer Profile Premium Redesign
- **Gradient header**: Transparent top bar over a full `Primary → PrimaryLight` gradient header containing the avatar, name, email, and role badge.
- **Avatar ring**: 108dp white ring around the avatar with a white camera button.
- **Icon containers**: All `ProfileMenuItem` and `ProfileMenuSwitchItem` rows now use 36dp rounded icon containers.
- **Gradient logout**: Full-width gradient button with `Error → Error.copy(0.7f)` horizontal gradient.

### Worker Dashboard Polish
- **Floating orbs**: `DashboardHeader` gained two floating decorative orbs for visual depth.

### Worker Self-Profile Polish
- **Floating orb**: Hero section gained a floating decorative orb.
- **Settings icon containers**: `SettingsRow` now uses 36dp rounded icon containers.
- **Gradient logout**: Full-width gradient button matching customer profile style.
- Role-specific navigation handling is centralized in `AppNavGraph.kt`.
- `NavRoutes` now includes dedicated endpoints for Customer Bookings and Profile management.

## UI Polish & Premium Design System
- **Animation Utilities**: `AnimationUtils.kt` provides reusable Compose animations: `AnimatedListItem` (fade + slide entrance with staggered delay), `AnimatedCounter` (number counting animation), `PulseDot` (attention indicator), and `CrossfadeContent` (smooth content transitions).
- **Shimmer Loaders**: `ShimmerEffect.kt` offers `ShimmerBox`, `ShimmerCard`, `ShimmerCircle`, and `ShimmerTextLine` for skeleton loading placeholders with smooth shimmer gradients.
- **Premium Components**: `PremiumCards.kt` contains `GradientCard`, `ElevatedSurfaceCard`, `StatCard`, `PremiumButton`, `PremiumOutlinedButton`, `EmptyStateView`, `StatusChip`, and `GlassSurface` — all following Material 3 with custom shadows, gradients, and press-scale animations.
- **WorkerCard Redesign**: `WorkerCard.kt` now features a gradient accent bar, animated press scale, premium typography (`Text1`, `Text3`), and improved visual hierarchy with `shadow` elevation.
- **Bottom Navigation**: `KaushalyaBottomNav.kt` redesigned with a floating pill-style layout. Active tab expands to show label with `AnimatedVisibility` (expand/shrink + fade), icon color transitions with `animateColorAsState`, and press-scale feedback via `animateFloatAsState`.

### Screen Improvements
- **HomeScreen**: Hero header features a 3-stop gradient (`PrimaryDark` → `Primary` → `PrimaryLight`), floating greeting animation (`rememberInfiniteTransition` wave offset), pulse notification badge, and a glassmorphic search bar with shadow. Category grid items have press-scale animations, colored shadows, and premium icon containers. `TopWorkerCard` uses gradient book buttons and shadow styling. `SectionHeader` now includes a gradient accent bar.
- **WorkerDashboard**: Header upgraded with wave animation on greeting, pulse notification badge, and glass earnings card. `StatsStrip` uses `PremiumStatCard` with gradient backgrounds, colored icon containers, and overlapping negative offset positioning. `EnhancedJobCard` features press scale, premium shadow tinted by status color, and gradient Accept/Decline action buttons.
- **WorkerProfileScreen**: Hero section has a 4-stop gradient, glass back button, larger avatar with government certification badge, and improved chip styling. `ProfileSection` includes gradient accent bar. `ServicePricingRow` uses premium surface cards with shadow and tinted price tags. `ReviewPreviewCard` restyled with shadow, rating badge, and improved typography. Bottom bar uses a gradient "Book Now" CTA button.
- **NotificationsScreen**: `NotificationCard` restyled with unread accent border (`Primary.copy(0.2f)`), larger icon containers, better typography (`Text1`, `Text3`, `Text4`), and an unread dot indicator with border highlight.

## Phase 4: Navbar, Profiles & Bookings Polish

### Customer Navbar Fix
- **Simplified architecture**: Replaced complex `onGloballyPositioned` sliding pill indicator with a simpler, more reliable weight-based `animateDpAsState` pill width approach matching the worker nav quality.
- **Perfect centering**: Each nav item uses `Modifier.weight(1f)` with `Arrangement.SpaceEvenly`, eliminating alignment drift.
- **Consistent animations**: Press scale spring (`stiffness=Spring.StiffnessMedium`), icon color tween, pill width spring animation. No more jitter or off-balance feel.

### Customer Home Hero Improvements
- **Greeting fix**: Changed "Namaskara" to "Namaste 👋" with better typography (`bodyLarge`, `FontWeight.Medium`).
- **Animation optimization**: Single shared `rememberInfiniteTransition` at the top of `HomeHeroHeader` instead of nested ones. All orbs and wave use one transition instance.
- **Smoother floating**: Orbs use `LinearEasing` with slower durations (6000ms, 7000ms) for organic motion. Wave uses `FastOutSlowInEasing` with 4000ms duration and 3px range for subtle, smooth movement.
- **Better hierarchy**: Added `Spacer(4.dp)` between greeting and headline for cleaner visual separation.

### Customer Profile Redesign
- **Gradient header**: Transparent top bar over `Primary → PrimaryLight` gradient with floating decorative orb.
- **Avatar ring**: 112dp white glass ring (`.background(Color.White.copy(0.15f))`) with 4dp padding around 104dp avatar.
- **Camera button**: White circle with `shadow(2.dp)` for elevation.
- **Role badge**: Glass surface with border stroke for premium feel.
- **Quick stats row**: Three `CustomerStatCard` components with icon containers, overlapping negative offset (`offset(y = (-16).dp)`), matching worker dashboard pattern.
- **Section cards**: Added `shadow(3.dp)`, `BorderStroke(1.dp, Border.copy(0.3f))`, and `tonalElevation = 1.dp` for subtle depth.
- **Icon containers**: All menu items use 36dp rounded icon containers with `Primary.copy(0.08f)` background.
- **Gradient logout**: Full-width `Error → Error.copy(0.7f)` horizontal gradient button.

### Worker Profile Redesign
- **Transparent top bar**: White title and edit icon over gradient header.
- **Glass avatar ring**: 108dp with 15% white opacity, 4dp padding, 100dp avatar.
- **Verified badge**: Slightly larger (30dp) with white border.
- **Typography hierarchy**: Name uses `headlineSmall` + `ExtraBold`, role uses `bodyLarge` + `Medium`.
- **Premium chips**: All chips have `BorderStroke(1.dp, Color.White.copy(0.2f))` for glass effect. Available chip uses cleaner green (`0xFF22C55E`).
- **Stats strip**: Three `WorkerStatCard` components with overlapping negative offset, matching customer profile pattern.
- **SectionBlock cards**: Added shadow, border stroke, and premium padding.
- **Settings icon containers**: 36dp rounded containers matching global pattern.
- **Gradient logout**: Same full-width gradient button as customer profile.

### Worker Bookings Redesign
- **EnhancedJobCard**:
  - Status-colored avatar placeholder: Customer initial in a 48dp circle with `statusBg` background and `statusColor` text.
  - Press animation via `MutableInteractionSource` + `collectIsPressedAsState` for proper interaction tracking.
  - Surface with `BorderStroke(1.dp, statusColor.copy(0.08f))` for subtle status-tinted border.
  - Premium amount badge: `PrimaryTint` container with rounded corners instead of plain text.
  - `JobInfoChip` renamed from `InfoChip` with slightly larger spacing.
- **BookingsTab**:
  - Tab count badges: Each tab shows a count pill (e.g., "Pending 3") with dynamic background color.
  - Improved empty state: 100dp rounded container with border, larger spacing, bold typography.
  - List items keyed by `booking.id` for stable animations.
  - Increased card spacing to 14dp and padding to `horizontal=16, vertical=16`.

### Theme Tokens
- All polished screens use centralized theme tokens: `Text1` (primary text), `Text2` (secondary), `Text3` (tertiary), `Text4` (hint/muted), `Border` (dividers), `Primary`, `PrimaryLight`, `PrimaryDark`, `PrimaryTint`, `PrimarySubtle` for consistent color usage.
- Typography upgraded to `titleLarge` for section headers, `headlineSmall` for hero names, and `labelMedium`/`bodyMedium` for improved readability.

## Phase 5: Theme System, Dark Mode Persistence & Global Consistency

### DataStore Theme Persistence
- **ThemePreferenceManager**: New singleton using `androidx.datastore.preferences` with `booleanPreferencesKey("dark_mode")`. Exposes `isDarkMode: Flow<Boolean>` and `suspend setDarkMode(Boolean)`.
- **ThemeState**: Now accepts a `ThemePreferenceManager` and `suspend toggle()` writes to DataStore before updating in-memory state.
- **KaushalyaTheme**: Accepts `preferenceManager` parameter. Uses `collectAsState` to read persisted preference. Falls back to `isSystemInDarkTheme()` on first launch.
- **MainActivity**: Injects `ThemePreferenceManager` via Hilt and passes to `KaushalyaTheme`.
- **DI**: `AppModule` companion object provides `ThemePreferenceManager` via `@Provides @Singleton`.

### Rich Dark Mode Color System
- **Background**: `0xFF02040A` — deepest black with micro blue tint (AMOLED-friendly).
- **Surface hierarchy**: 5 levels — `DarkSurface` (`0xFF0B1221`), `DarkSurfaceVariant` (`0xFF141D2E`), `DarkSurfaceContainer` (`0xFF111B2E`), `DarkSurfaceContainerHigh` (`0xFF16233A`), `DarkSurfaceContainerHighest` (`0xFF1C2D48`).
- **Contrast**: `DarkOnBackground` (`0xFFF1F5F9`), `DarkOnSurface` (`0xFFF8FAFC`) for crisp text.
- **Borders**: `DarkOutline` (`0xFF2A3A52`), `DarkOutlineVariant` (`0xFF1A2538`), `DarkCardBorder` (`0xFF1E3A5F` at 40% opacity).
- **Avoids flat gray**: All dark surfaces have subtle blue tint for a rich, premium feel.

### Premium Navbar Animations
- **Gradient pill**: Active item uses `Brush.horizontalGradient(listOf(Primary, PrimaryLight))` instead of solid color.
- **Icon scale**: `animateFloatAsState` from `1.0f → 1.15f` on selection with spring (`stiffness=400f, damping=0.7`).
- **Spring physics**: Pill width uses `stiffness=320f, dampingRatio=0.75f`. Label expand uses `spring(stiffness=400f)`.
- **Press feedback**: Scale to `0.92f` on press with spring.

### Worker Dark Mode Toggle
- **SettingsSwitchRow**: New composable using Material3 `Switch` with custom `SwitchDefaults.colors` (checkedThumb = Primary, checkedTrack = Primary.copy(0.5f)).
- **Proper state handling**: `themeState.toggle()` wrapped in `rememberCoroutineScope().launch` across all screens (CustomerProfile, HomeScreen, WorkerSelfProfile).

### Timeline Booking List
- Each `EnhancedJobCard` wrapped in a `Row` with a timeline column on the left.
- **Status dots**: 12dp circles colored by booking status with white border.
- **Connector lines**: 2dp vertical lines between cards, colored with status color at 30% opacity.
- Creates a visual journey/progress feeling for the worker.

### Logo Branding
- `logo.png` copied to `res/drawable/logo.png`.
- **WelcomeScreen**: Replaced Handshake icon with `Image(painter = painterResource(R.drawable.logo))` inside the glass container with pulse animation.
- **AuthScreen**: 36dp logo placed in header next to back button.

## Phase 6: AI Review Summary (Customer Side)

### Secure API Key Management
- **local.properties**: `OPENROUTER_API_KEY` stored in gitignored `local.properties`.
- **BuildConfig injection**: `app/build.gradle.kts` reads the key at build time via `Properties()` and injects into `BuildConfig.OPENROUTER_API_KEY` via `buildConfigField`. The key never appears in source code.

### OpenRouter API Integration
- **Ktor client**: `OpenRouterService` uses the existing `ktor-client-android` dependency.
- **Model**: `openai/gpt-oss-120b` (free tier on OpenRouter).
- **System prompt**: Enforces 1-3 line summaries, natural language, no hallucinations, only real review patterns.
- **Review formatting**: Reviews formatted as `[★★★☆☆] comment text` for structured input.
- **Headers**: `Authorization: Bearer {key}`, `HTTP-Referer`, `X-Title` for OpenRouter compliance.

### DataStore Caching
- **Cache key**: `stringPreferencesKey("summary_{workerId}")` for summary text, `longPreferencesKey("summary_ts_{workerId}")` for timestamp.
- **TTL**: 24 hours (`CACHE_DURATION_MS = 86_400_000L`).
- **Behavior**: Returns cached summary immediately if within TTL. Only calls API on cache miss or expiry.
- **Minimum threshold**: Requires at least 2 reviews before attempting generation.

### UI States (AiReviewSummaryCard)
- **Loading**: `ShimmerLine` placeholders (2 lines) with shimmer animation.
- **Success**: Fade-in text with gradient-bordered card, sparkle icon in glass container.
- **Error (API)**: Warning icon + "Unable to generate summary" + Retry button.
- **Error (not enough reviews)**: Warning icon + "Not enough reviews yet" (no retry).
- **Dark mode**: Uses `MaterialTheme.colorScheme.surface`, `Text1`, `Text2`, `Primary` tokens for automatic adaptation.

### ViewModel & Integration
- **AiSummaryViewModel**: `@HiltViewModel` with `UiState<String>` exposed as `StateFlow`. Methods: `generateSummary(workerId, reviews)`, `retry(workerId, reviews)`, `loadCachedSummary(workerId)`.
- **WorkerProfileScreen**: Injected alongside `WorkerProfileViewModel`. Card placed inside `ProfileSection(title = "Reviews")` below the rating summary row. `LaunchedEffect(rv.data)` triggers generation when reviews load.

## Phase 2: Premium Dark Mode & Navigation Overhaul

### Dark Mode Overhaul
- **AMOLED-friendly backgrounds**: `DarkBackground` (`0xFF030712`) — near-black with subtle blue tint.
- **Rich tonal surfaces**: 5 surface levels (`DarkSurface`, `DarkSurfaceVariant`, `DarkSurfaceContainer`, `DarkSurfaceContainerHigh`, `DarkSurfaceContainerHighest`) for proper elevation hierarchy in dark mode.
- **Better contrast**: `DarkOnBackground` (`0xFFE2E8F0`) and `DarkOnSurface` (`0xFFF1F5F9`) for crisp text readability.
- **Premium scrim**: Deepened to `0xE6000000` for elegant modal overlays.
- **All Material 3 dark scheme tokens**: `inverseSurface`, `inverseOnSurface`, `inversePrimary`, `surfaceTint`, `surfaceDim`, `surfaceBright`, and the full surface container API are now properly defined.

### Typography System Upgrade
- **Google Fonts**: `Poppins` (display/headline), `Inter` (body), and `Manrope` (titles/labels) create a premium 3-font hierarchy.
- **Title hierarchy**: `titleLarge` uses Manrope Bold 20sp with negative letter-spacing for modern headlines.
- **Body readability**: `bodyLarge` increased to 16sp with 26sp line-height and positive letter-spacing for comfortable reading.
- **Label hierarchy**: `labelLarge` and `labelMedium` use Manrope for card labels and buttons, creating visual distinction from body text.

### Navigation Fixes
- **Search/Explore tab**: `SearchScreen` now includes `KaushalyaBottomNav` and receives `onNavigateBottomBar`, making it a proper persistent tab instead of a standalone page that hid the navbar.
- **Seamless tab switching**: HomeScreen search bar click and category click now navigate to the Search tab via `handleCustomerNav` with `saveState`/`restoreState`, maintaining state across tab switches.
- **Worker bottom nav**: `WorkerBottomNav.kt` completely redesigned to match the customer pill-style nav — floating rounded surface, spring-based pill width expand/collapse, `AnimatedVisibility` label reveal, press-scale feedback, and shadow elevation.

### Bookings UI Polish
- **CustomerBookingCard**: Complete redesign with status-colored icon containers (48dp rounded squares with tinted backgrounds), gradient amount badges, `StatusPanel` component for status alerts, premium `InfoChip` with icon containers, and rounded action buttons.
- **Customer empty state**: Premium 96dp rounded container with `Primary.copy(0.06f)` background and descriptive subtext.
- **Worker BookingsTab**: Empty state upgraded with the same premium pattern. `InfoChip` updated with icon containers across the entire worker dashboard.

### Animation Improvements
- **ElasticPressEffect**: New `AnimationUtils.kt` composable providing spring-based elastic press animations (`stiffness=400f`, `dampingRatio=0.35f`) for tactile button/card interactions.
- **ShimmerSweep**: New reusable shimmer sweep animation using `rememberInfiniteTransition` with `LinearEasing` for smooth loading placeholders.

## State Management
- All new screens utilize a consistent `UiState` pattern for loading, success, and error handling.
- `ProfileViewModel` now manages both avatar uploads and profile metadata updates in a single reactive flow.
