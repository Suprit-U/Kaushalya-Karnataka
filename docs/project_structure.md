# Project Structure Guide

Last updated: 2026-05-06

The Kaushalya-Karnataka app follows an MVVM structure with a repository-based data layer and Hilt dependency injection.

## Root Files

- `settings.gradle.kts`: Gradle plugin management and module inclusion.
- `build.gradle.kts`: Project-level Gradle configuration.
- `gradle/libs.versions.toml`: Central dependency and plugin versions.
- `gradle.properties`: JVM, AndroidX, Jetifier, Java home, and compileSdk warning configuration.
- `app/build.gradle.kts`: Android app module configuration.
- `app/proguard-rules.pro`: Project-specific R8 rules file referenced by release builds.
- `app/google-services.json`: Firebase Android configuration.
- `Design-Prototype.html`: Original visual/prototype reference.

## Main App Package

Root package:

```text
app/src/main/java/com/kaushalyakarnataka/app/
```

### `data/`

Data models, Firebase constants, and local database definitions.

- `model/`: Domain models (User, Worker, Service, Booking, Review, Notification).
- `repository/`: Repository implementations (Auth, Worker, Booking, Review, Service, Storage, Notification).
- `firebase/`: Firebase configuration and Firestore constants.
- `local/`: Room database configuration.
  - `LocalDatabase.kt`: Consolidated Room entities, DAOs, and database class.

### `di/`

Hilt dependency injection modules.

- `AppModule.kt`: Binds repository interfaces to implementations.
- `DatabaseModule.kt`: Provides Room database and DAO instances.
- `SupabaseModule.kt`: Provides Supabase client and storage.

### `navigation/`

Navigation Compose route definitions and graph wiring.

- `NavRoutes.kt`: Route strings and argument names.
- `AppNavGraph.kt`: Auth, customer, and worker navigation flows.

### `ui/`

Compose UI code.

#### `ui/components/`

Reusable components:

- `AppTopBar.kt`
- `AvatarComponent.kt`
- `BadgeComponent.kt`
- `KaushalyaBottomNav.kt`: Custom animated bottom navigation.
- `Buttons.kt`
- `CategoryGrid.kt`
- `ChipRow.kt`
- `DatePickerRow.kt`
- `EmptyState.kt`
- `HireSuccessModal.kt`
- `InputField.kt`
- `JobCard.kt`
- `PortfolioGrid.kt`
- `PromoBanner.kt`
- `QuickActionGrid.kt`
- `RatingBar.kt`
- `RatingBreakdown.kt`
- `ReviewCard.kt`
- `SearchResultCard.kt`
- `ServicePricingCard.kt`
- `SkeletonLoader.kt`
- `TimeSlotPicker.kt`
- `WorkerCard.kt`
- `SearchBarField.kt`
- `CategoryChipRow.kt`

#### `ui/screens/`

Full screens categorized by user role:

- `auth/`: Authentication flow (Welcome, Role Selection, Auth Screen).
- `customer/`: Customer-specific screens.
  - `HomeScreen.kt`: Personalized dashboard with trending experts and categories.
  - `SearchScreen.kt`: Advanced search and filtering.
  - `WorkerProfileScreen.kt`: Detailed worker profile view for customers.
  - `HireRequestScreen.kt`: Booking and scheduling flow.
  - `ReviewsScreen.kt`: Detailed review listings.
  - `CustomerBookingsScreen.kt`: Customer's booking history and status.
  - `CustomerProfileScreen.kt`: Customer profile management.
- `worker/`: Worker-specific screens.
  - `WorkerDashboardScreen.kt`: Real-time job requests and earnings overview.
  - `WorkerSelfProfileScreen.kt`: Worker's own profile management.
  - `AddServiceScreen.kt`: Service management for workers.
  - `PortfolioScreen.kt`: Portfolio management.
- `common/`: Shared screens like `LoadingScreen.kt` and `NotificationsScreen.kt`.

#### `ui/theme/`

Material theme configuration:

- `Color.kt`
- `Dimens.kt`
- `Shape.kt`
- `Theme.kt`
- `Typography.kt`

### `utils/`

Shared helpers:

- `UiState.kt`
- `DateUtils.kt`
- `CurrencyUtils.kt`
- `Extensions.kt`

### `viewmodel/`

Screen-level state management and business logic:

- `AuthViewModel.kt`: Global authentication and session state.
- `HomeViewModel.kt`: Customer home feed logic.
- `SearchViewModel.kt`: Search and filtering logic.
- `WorkerProfileViewModel.kt`: Detailed profile and reviews logic.
- `HireViewModel.kt`: Booking creation and validation.
- `WorkerDashboardViewModel.kt`: Job management for workers.
- `CustomerBookingsViewModel.kt`: Booking history management.
- `ProfileViewModel.kt`: User profile updates and avatar uploads.
- `NotificationViewModel.kt`: Notifications management.

## Resources

```text
app/src/main/res/
```

- `values/strings.xml`: String resources retained for localization.
- `values/colors.xml`: XML color resources.
- `values/themes.xml`: Android view/system theme definitions.

Most runtime UI strings are currently defined directly in Compose code.

## Generated and Build Output

Generated files under these folders should not be edited manually:

- `.gradle/`
- `.kotlin/`
- `build/`
- `app/build/`

## Adding New Work

1. Add model/repository code under `data/` when the feature needs persistence.
2. Bind new repository implementations in `di/AppModule.kt`.
3. Add ViewModel state and actions under `viewmodel/`.
4. Build reusable UI in `ui/components/`.
5. Add screens under `ui/screens/<area>/`.
6. Add routes in `NavRoutes.kt` and wire them in `AppNavGraph.kt`.
7. Run `.\gradlew.bat build`.
