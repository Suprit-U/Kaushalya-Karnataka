# Kaushalya Karnataka - Service Marketplace App

Kaushalya Karnataka is a modern, high-performance Android application designed to bridge the gap between skilled workers and customers in Karnataka. Built with Jetpack Compose and a robust MVVM architecture, the app provides a seamless experience for hiring experts, managing bookings, and tracking professional portfolios.

## 🚀 Key Features

### For Customers
- **Personalized Home Feed**: Discover top-rated experts and trending services in your local area.
- **Advanced Search & Discovery**: Filter workers by category, rating, experience, and service type.
- **Seamless Booking Flow**: Schedule services with integrated date and time slot pickers.
- **Real-time Notifications**: Stay updated on booking status changes and service alerts.
- **Profile & Booking Management**: Track your service history and manage your personal profile.
- **Reviews & Ratings**: Share feedback and view detailed worker reviews from the community.

### For Workers
- **Professional Dashboard**: Monitor job requests, track earnings, and manage upcoming bookings in real-time.
- **Service Management**: Easily add, edit, and categorize the services you offer.
- **Dynamic Portfolio**: Showcase your work with a dedicated portfolio gallery.
- **Verification & Certification**: Build trust with verified badges and government certification markers.
- **Availability Toggle**: Control your working status with a simple online/offline switch.

## 🛠 Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Kotlin)
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Backend (Auth & DB)**: [Firebase Authentication](https://firebase.google.com/docs/auth) & [Cloud Firestore](https://firebase.google.com/docs/firestore)
- **Image Storage**: [Supabase Storage](https://supabase.com/docs/guides/storage)
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) with custom slide/fade transitions
- **Asynchronous Logic**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)

## 🏗 Project Structure

```text
app/src/main/java/com/kaushalyakarnataka/app/
├── data/           # Models, Repositories, and Data Sources (Firebase, Room)
├── di/             # Hilt Modules for Dependency Injection
├── navigation/     # AppNavGraph and Route Definitions
├── ui/             # UI Layer
│   ├── components/ # Reusable UI Components (Custom TopBar, BottomNav, etc.)
│   ├── screens/    # Full-screen Composables (Auth, Customer, Worker flows)
│   └── theme/      # Material 3 Design System (Color, Typography, Shapes)
├── utils/          # Shared Helpers and Extensions
└── viewmodel/      # Business Logic and State Management
```

## 📄 Documentation

Comprehensive documentation is available in the `docs/` folder:
- [Project Structure Guide](docs/project_structure.md)
- [Firebase Setup Guide](docs/firebase_setup.md)
- [Supabase Storage Guide](docs/supabase/setup_guide.md)
- [Stabilization Walkthrough](docs/walkthrough.md)
- [Development Task List](docs/task.md)

## 🔧 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Suprit-U/Kaushalya-Karnataka.git
   ```
2. **Setup Firebase**:
   - Place your `google-services.json` in the `app/` directory.
   - Enable Auth and Firestore in the Firebase Console.
3. **Setup Supabase**:
   - Update credentials in `SupabaseModule.kt` for image storage.
4. **Build and Run**:
   - Open the project in Android Studio (Ladybug or later).
   - Sync Gradle and run on an emulator or physical device.

## 🤝 Contribution

This project follows modern clean code principles. For adding new features:
1. Follow the MVVM structure.
2. Ensure dependency injection is used for all services.
3. Keep the UI layer reactive using `StateFlow`.
4. Run `.\gradlew.bat build` before committing.

---

*Built with ❤️ for a Skilled Karnataka.*
