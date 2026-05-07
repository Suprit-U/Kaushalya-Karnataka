# Firebase Setup Guide

Last updated: 2026-05-06

This guide explains the Firebase setup expected by the current Android app.

## Current App Integration

The Android project is already wired for Firebase:

- `app/google-services.json` exists.
- Google Services Gradle plugin is applied.
- Firebase Auth and Firestore dependencies are configured.
- `FirebaseModule.kt` provides Firebase instances through Hilt.
- Repository implementations use Firebase Auth and Firestore.

The app builds successfully with:

```powershell
.\gradlew.bat build
```

## 1. Firebase Project

Status: done for this local project if `app/google-services.json` belongs to your Firebase project.

Expected Firebase project name:

```text
Kaushalya-Karnataka
```

## 2. Android App Registration

Expected package name:

```text
com.kaushalyakarnataka.app
```

For Google Sign-In or phone authentication, add the debug SHA-1/SHA-256 fingerprints in Firebase Console.

To view signing fingerprints:

```powershell
.\gradlew.bat signingReport
```

## 3. Required Firebase Products

Enable these in Firebase Console:

- Authentication
- Firestore Database

Recommended auth providers for current app flow:

- Email/password
- Phone, if phone auth is required for your target flow
- Google, if Google sign-in is added or enabled later

## 4. Firestore Collections

Collection constants are defined in:

```text
app/src/main/java/com/kaushalyakarnataka/app/data/firebase/FirestoreCollections.kt
```

### `users`

Document ID:

```text
{uid}
```

Common fields:

- `uid`
- `name`
- `phone`
- `email`
- `role`
- `avatarUrl`
- `location`
- `createdAt`

### `workers`

Document ID:

```text
{uid}
```

Common fields:

- `uid` (String)
- `name` (String)
- `category` (String)
- `role` (String)
- `bio` (String)
- `rating` (Double)
- `reviewCount` (Int)
- `experienceYears` (Int)
- `successRate` (Int)
- `pricePerHour` (Int)
- `distanceKm` (Double)
- `isAvailable` (Boolean)
- `isVerified` (Boolean)
- `isGovernmentCertified` (Boolean)
- `tags` (String/Array)
- `avatarUrl` (String)
- `phone` (String)
- `location` (String)

### `bookings`

Document ID:

```text
{bookingId}
```

Common fields:

- `id` (String)
- `customerId` (String)
- `customerName` (String)
- `workerId` (String)
- `workerName` (String)
- `service` (String)
- `scheduledDate` (Timestamp/Long)
- `timeSlot` (String)
- `address` (String)
- `notes` (String)
- `status` (String)
- `estimatedCostMin` (Int)
- `estimatedCostMax` (Int)
- `bookingCode` (String)
- `createdAt` (Timestamp/Long)

Valid booking statuses:

- `PENDING`
- `CONFIRMED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### `reviews`

Document ID:

```text
{reviewId}
```

Common fields:

- `workerId`
- `customerId`
- `customerName`
- `customerInitial`
- `customerAvatarUrl`
- `rating`
- `comment`
- `serviceType`
- `photoUrls`
- `helpfulCount`
- `isVerified`
- `createdAt`

### `services`

Document ID:

```text
{serviceId}
```

Common fields:

- `workerId`
- `name`
- `category`
- `description`
- `startingPrice`
- `pricingType`
- `estimatedDuration`
- `tags`
- `isActive`

Valid pricing types:

- `HOURLY`
- `FIXED`
- `STARTING_AT`

Valid service durations:

- `ONE_HOUR`
- `TWO_TO_THREE_HOURS`
- `FULL_DAY`

### `notifications`

Document ID:

```text
{notificationId}
```

Common fields:

- `id` (String)
- `userId` (String) — recipient UID
- `title` (String)
- `message` (String)
- `type` (String) — `BOOKING_REQUEST`, `BOOKING_CONFIRMED`, `BOOKING_DECLINED`, `BOOKING_COMPLETED`, `NEW_REVIEW`, `BOOKING_UPDATE`
- `bookingId` (String)
- `isRead` (Boolean)
- `createdAt` (Timestamp)

**Required composite index:**
```
Collection: notifications
Fields:
  - userId (Ascending)
  - createdAt (Descending)
```

### `workers/{workerId}/portfolio`

Worker portfolio items are loaded from this subcollection.

Common fields:

- `id`
- `workerId`
- `photoUrl`
- `caption`
- `serviceCategory`

## 5. Development Firestore Rules

For local development, this simple authenticated-user rule is enough to exercise the current repository flows:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Do not use this rule set unchanged for production.

## 6. Production Rule Direction

Before production, tighten rules so users can only edit their own documents and allowed booking/review records.

Recommended ownership patterns:

- Users can read public worker profile data.
- Users can update only `users/{request.auth.uid}`.
- Workers can update only `workers/{request.auth.uid}`.
- Customers can create bookings where `customerId == request.auth.uid`.
- Workers can update booking status only for bookings assigned to them.
- Reviews should only be created by authenticated customers for completed bookings.

## 7. Firebase Storage

Firebase Storage is not used by the current app. Image storage uses Supabase Storage instead. See:

```text
docs/supabase/setup_guide.md
```

## 8. Required Firestore Composite Indexes

The app includes `firestore.indexes.json` for **production performance optimization**, but the notification system has been restructured to work **without requiring deployed composite indexes**.

### How Notification Queries Work (No Index Required)

All notification queries now use only **single-field equality filters** on `userId`, which Firestore indexes automatically. Client-side sorting and filtering replace compound queries:

| Query | Old (needed composite index) | New (single-field only) |
|-------|------------------------------|-------------------------|
| List notifications | `whereEqualTo(userId) + orderBy(createdAt DESC)` | `whereEqualTo(userId)` → sort client-side |
| Mark all read | `whereEqualTo(userId) + whereEqualTo(isRead)` | `whereEqualTo(userId)` → filter client-side |
| Unread count | `whereEqualTo(userId) + whereEqualTo(isRead)` | `whereEqualTo(userId)` → count client-side |

This means the app works **immediately** after Firebase project setup, without waiting for index builds.

### Recommended Production Indexes

For better performance at scale, deploy these optional indexes:

```text
firestore.indexes.json
```

| Collection   | Fields                                    | Purpose                          |
|--------------|-------------------------------------------|----------------------------------|
| notifications| userId (ASC) → createdAt (DESC)           | Optimized notification list      |
| workers      | category (ASC) → rating (DESC)           | Search with category + sort      |
| reviews      | workerId (ASC) → createdAt (DESC)         | Worker review stream             |
| bookings     | workerId (ASC) → createdAt (DESC)        | Worker booking history           |
| bookings     | customerId (ASC) → createdAt (DESC)      | Customer booking history         |

Deploy indexes:

```powershell
firebase deploy --only firestore:indexes
```

## 9. Troubleshooting

- App fails on startup: verify `app/google-services.json` is present and matches `com.kaushalyakarnataka.app`.
- Login/sign-up fails: verify Authentication is enabled and the provider is configured.
- Firestore reads/writes fail: verify Firestore exists and rules allow the current authenticated user.
- **No notifications appearing**: Check Logcat for `NotificationRepository` tags. Ensure the notifications collection exists and documents have a `userId` field matching the current user's UID.
- Empty UI data: repositories may show sample fallback data when Firestore fails; check Logcat and Firebase Console.
