package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.User
import com.kaushalyakarnataka.app.data.model.UserRole
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "AuthRepository"

interface AuthRepository {
    val currentUser: FirebaseUser?
    val currentUserFlow: Flow<FirebaseUser?>
    /**
     * Signs in and validates that the account's role matches [expectedRole].
     * Pass null for [expectedRole] to skip role validation (e.g., auto-login on startup).
     */
    suspend fun signIn(email: String, password: String, expectedRole: UserRole? = null): UiState<User>
    suspend fun signUp(name: String, email: String, password: String, phone: String, role: UserRole): UiState<User>
    suspend fun signOut()
    suspend fun getUserProfile(uid: String): UiState<User>
    suspend fun updateUserProfile(uid: String, name: String, phone: String, location: String = ""): UiState<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String, expectedRole: UserRole?): UiState<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return UiState.Error("Authentication failed. Please try again.")

            val profileResult = getUserProfile(uid)
            if (profileResult is UiState.Error) {
                // User exists in Firebase Auth but not in Firestore — create a basic profile
                // This handles pre-existing worker accounts that were created without Firestore docs
                Log.w(TAG, "User $uid has no Firestore profile, attempting to infer role")
                return handleMissingProfile(uid, email, expectedRole)
            }

            val user = (profileResult as UiState.Success).data

            // Role-based portal validation
            if (expectedRole != null && user.role != expectedRole) {
                auth.signOut()
                val portalName = if (expectedRole == UserRole.CUSTOMER) "Customer" else "Worker"
                val accountType = if (user.role == UserRole.CUSTOMER) "Customer" else "Worker"
                return UiState.Error(
                    "This account belongs to a $accountType. Please use the $accountType Login portal."
                )
            }

            UiState.Success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            UiState.Error("No account found with this email. Please sign up first.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            UiState.Error("Incorrect password. Please try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            UiState.Error(e.message ?: "Sign in failed. Please try again.")
        }
    }

    /**
     * Called when Firebase Auth user exists but no Firestore document found.
     * Attempts to identify worker accounts by email pattern and create their profile.
     */
    private suspend fun handleMissingProfile(uid: String, email: String, expectedRole: UserRole?): UiState<User> {
        // Known worker email patterns from test accounts
        val workerEmails = setOf(
            "electrician1@gmail.com", "plumber1@gmail.com", "carpenter1@gmail.com",
            "painter1@gmail.com", "acrepair1@gmail.com", "geyserrepair1@gmail.com",
            "motorrepair1@gmail.com", "appliancefix1@gmail.com"
        )
        val inferredRole = when {
            email in workerEmails -> UserRole.WORKER
            expectedRole != null -> expectedRole
            else -> UserRole.CUSTOMER
        }

        // Role check before creating profile
        if (expectedRole != null && inferredRole != expectedRole) {
            auth.signOut()
            val accountType = if (inferredRole == UserRole.CUSTOMER) "Customer" else "Worker"
            val portalName = if (expectedRole == UserRole.CUSTOMER) "Customer" else "Worker"
            return UiState.Error(
                "This account belongs to a $accountType. Please use the $accountType Login portal."
            )
        }

        val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val user = User(
            uid = uid,
            name = displayName,
            email = email,
            phone = "",
            role = inferredRole,
        )

        return try {
            // Create the missing Firestore document
            firestore.collection(FirestoreCollections.USERS).document(uid).set(user).await()

            // If worker, also create worker document
            if (inferredRole == UserRole.WORKER) {
                createWorkerDocument(uid, user)
            }
            Log.i(TAG, "Created missing Firestore profile for $uid with role $inferredRole")
            UiState.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create missing profile", e)
            // Still return success with the inferred user — auth succeeded
            UiState.Success(user)
        }
    }

    private suspend fun createWorkerDocument(uid: String, user: User) {
        try {
            val existing = firestore.collection(FirestoreCollections.WORKERS).document(uid).get().await()
            if (!existing.exists()) {
                val workerData = mapOf(
                    "uid" to uid,
                    "name" to user.name,
                    "email" to user.email,
                    "category" to "OTHER",
                    "role" to "Skilled Worker",
                    "bio" to "",
                    "rating" to 0.0,
                    "reviewCount" to 0,
                    "experienceYears" to 0,
                    "successRate" to 0,
                    "pricePerHour" to 0,
                    "distanceKm" to 0.0,
                    "isAvailable" to true,
                    "isVerified" to false,
                    "isGovernmentCertified" to false,
                    "tags" to emptyList<String>(),
                    "avatarUrl" to "",
                    "phone" to "",
                    "location" to "",
                )
                firestore.collection(FirestoreCollections.WORKERS).document(uid).set(workerData).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not create worker document: ${e.message}")
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole,
    ): UiState<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return UiState.Error("Account creation failed")

            val user = User(uid = uid, name = name, email = email, phone = phone, role = role)

            firestore.collection(FirestoreCollections.USERS).document(uid).set(user).await()

            if (role == UserRole.WORKER) {
                createWorkerDocument(uid, user)
            }

            UiState.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed", e)
            UiState.Error(e.message ?: "Sign up failed. Please try again.")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getUserProfile(uid: String): UiState<User> {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
            if (!snapshot.exists()) {
                return UiState.Error("User profile not found")
            }
            val user = snapshot.toObject(User::class.java)
                ?: return UiState.Error("Failed to parse user profile")
            UiState.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "getUserProfile failed", e)
            UiState.Error(e.message ?: "Failed to load profile")
        }
    }

    override suspend fun updateUserProfile(uid: String, name: String, phone: String, location: String): UiState<Unit> {
        return try {
            firestore.collection(FirestoreCollections.USERS).document(uid)
                .update(mapOf("name" to name, "phone" to phone, "location" to location))
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update profile")
        }
    }
}
