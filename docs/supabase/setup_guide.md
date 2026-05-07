# Supabase Storage Setup Guide

Last updated: 2026-05-06

The app uses Firebase for Authentication and Firestore, and Supabase only for image storage.

## Current App Integration

Supabase is already integrated in code:

- Dependency: Supabase Storage Kotlin SDK.
- Hilt provider: `app/src/main/java/com/kaushalyakarnataka/app/di/SupabaseModule.kt`
- Repository: `app/src/main/java/com/kaushalyakarnataka/app/data/repository/StorageRepository.kt`
- Bucket name used by the app: `kaushalya-storage`

The app builds successfully with:

```powershell
.\gradlew.bat build
```

## Project Details

Current configured project:

- Project ID: `ytuosxjwpvsxwtnoppjm`
- Project URL: `https://ytuosxjwpvsxwtnoppjm.supabase.co`
- Bucket: `kaushalya-storage`

The anon public API key is currently configured in `SupabaseModule.kt`.

Production recommendation: move the Supabase URL and anon key into build configuration instead of keeping them directly in source.

## 1. Create or Verify the Supabase Project

1. Open the Supabase dashboard.
2. Select the project with ID `ytuosxjwpvsxwtnoppjm`, or create a new project if you are replacing the backend.
3. If replacing the project, update `SUPABASE_URL` and `SUPABASE_KEY` in `SupabaseModule.kt`.

## 2. Create or Verify the Storage Bucket

Bucket name expected by the app:

```text
kaushalya-storage
```

Bucket visibility:

```text
Public
```

The app calls `bucket.publicUrl(path)`, so uploaded images must be publicly readable unless the app is later changed to signed URLs.

## 3. Storage Policies

Because Firebase Auth is used for app authentication, Supabase does not know the Firebase user identity. The current hybrid setup uses broad storage policies for development.

Run these policies in Supabase SQL Editor if the bucket is not already configured:

```sql
CREATE POLICY "Public Read Access"
ON storage.objects FOR SELECT
USING (bucket_id = 'kaushalya-storage');

CREATE POLICY "Allow Uploads"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'kaushalya-storage');

CREATE POLICY "Allow Updates"
ON storage.objects FOR UPDATE
USING (bucket_id = 'kaushalya-storage');

CREATE POLICY "Allow Deletes"
ON storage.objects FOR DELETE
USING (bucket_id = 'kaushalya-storage');
```

These policies are convenient for development but should be tightened before production.

## 4. Current Repository Behavior

`StorageRepository.uploadImage(byteArray, path)`:

- Generates a unique JPG filename.
- Uploads with `upsert = true`.
- Returns a public URL in `UiState.Success`.
- Returns `UiState.Error` on failure.

`StorageRepository.deleteImage(path)`:

- Deletes the requested object path from the bucket.
- Returns `UiState.Success(Unit)` on success.

## 5. Path Conventions

Path helper functions are defined in `FirestoreCollections.kt` under `StoragePaths`:

```kotlin
avatarPath(uid)
portfolioPath(uid, fileName)
reviewPhotoPath(bookingId, fileName)
```

The repository also accepts direct path prefixes, so callers can upload into feature-specific folders.

## 6. Production Hardening

Before production:

- Move Supabase credentials out of source.
- Avoid public write/delete policies.
- Consider a backend service that validates Firebase ID tokens and creates signed upload URLs.
- Store only public URLs or object paths in Firestore, depending on your access strategy.
- Add upload size/type validation.

## 7. Troubleshooting

- Upload fails: verify bucket exists and insert policy is active.
- Image URL does not load: verify bucket is public or switch to signed URL loading.
- Delete fails: verify delete policy allows the object path.
- Build fails after changing SDK versions: check `gradle/libs.versions.toml` and run `.\gradlew.bat build`.
