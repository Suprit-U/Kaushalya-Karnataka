package com.kaushalyakarnataka.app.data.repository

import com.google.firebase.auth.FirebaseAuth
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

/**
 * Interface for authentication operations.
 * Abstracts Firebase Auth so ViewModels don't depend on Firebase directly.
 */
interface AuthRepository {
    val currentUser: FirebaseUser?
    val currentUserFlow: Flow<FirebaseUser?>
    suspend fun signIn(email: String, password: String): UiState<User>
    suspend fun signUp(name: String, email: String, password: String, phone: String, role: UserRole): UiState<User>
    suspend fun signOut()
    suspend fun getUserProfile(uid: String): UiState<User>
}

/**
 * Firebase implementation of AuthRepository.
 * Handles email/password auth and Firestore user profile creation.
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): UiState<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return UiState.Error("Authentication failed")
            getUserProfile(uid)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Sign in failed. Please try again.")
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

            val user = User(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
            )

            // Save user profile to Firestore
            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .set(user)
                .await()

            UiState.Success(user)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Sign up failed. Please try again.")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getUserProfile(uid: String): UiState<User> {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()
            val user = snapshot.toObject(User::class.java)
                ?: return UiState.Error("User profile not found")
            UiState.Success(user)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load profile")
        }
    }
}
