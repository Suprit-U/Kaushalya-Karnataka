package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "WorkerRepository"

interface WorkerRepository {
    suspend fun getTopWorkers(limit: Long = 10): UiState<List<Worker>>
    suspend fun getWorkerById(uid: String): UiState<Worker>
    fun observeWorkerById(uid: String): Flow<UiState<Worker>>
    suspend fun searchWorkers(query: String, category: ServiceCategory? = null): UiState<List<Worker>>
    suspend fun getWorkersByCategory(category: ServiceCategory): UiState<List<Worker>>
    suspend fun updateAvailability(uid: String, isAvailable: Boolean): UiState<Unit>
    suspend fun updateWorkerProfile(uid: String, updates: Map<String, Any>): UiState<Unit>
    suspend fun updateWorkerProfileFields(
        uid: String,
        name: String,
        phone: String,
        bio: String,
        role: String,
        category: ServiceCategory,
        baseLabourCharge: Int
    ): UiState<Unit>
}

class WorkerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : WorkerRepository {

    private val workersRef get() = firestore.collection(FirestoreCollections.WORKERS)

    override suspend fun getTopWorkers(limit: Long): UiState<List<Worker>> {
        return try {
            val snapshot = workersRef
                .orderBy(FirestoreCollections.Fields.RATING, Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
            UiState.Success(workers)
        } catch (e: Exception) {
            Log.e(TAG, "getTopWorkers failed", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun getWorkerById(uid: String): UiState<Worker> {
        return try {
            val snapshot = workersRef.document(uid).get().await()
            val worker = parseWorker(snapshot)
            if (worker != null) UiState.Success(worker)
            else UiState.Error("Worker not found")
        } catch (e: Exception) {
            Log.e(TAG, "getWorkerById failed for $uid", e)
            UiState.Error(e.message ?: "Worker not found")
        }
    }

    override fun observeWorkerById(uid: String): Flow<UiState<Worker>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(UiState.Error("Worker not found"))
            close()
            return@callbackFlow
        }
        val registration = workersRef.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(UiState.Error(error.message ?: "Failed to load worker"))
                return@addSnapshotListener
            }
            val worker = snapshot?.let { parseWorker(it) }
            trySend(if (worker != null) UiState.Success(worker) else UiState.Error("Worker not found"))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun searchWorkers(query: String, category: ServiceCategory?): UiState<List<Worker>> {
        return try {
            val snapshot = if (category != null) {
                workersRef
                    .whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
                    .get().await()
            } else {
                workersRef
                    .orderBy(FirestoreCollections.Fields.RATING, Query.Direction.DESCENDING)
                    .limit(100)
                    .get().await()
            }

            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
                .filter { worker ->
                    query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.role.contains(query, ignoreCase = true) ||
                    worker.category.displayName.contains(query, ignoreCase = true) ||
                    worker.tags.any { it.contains(query, ignoreCase = true) }
                }
                .sortedByDescending { it.rating }
            UiState.Success(workers)
        } catch (e: Exception) {
            Log.e(TAG, "searchWorkers failed", e)
            UiState.Error(e.message ?: "Search failed. Please try again.")
        }
    }

    override suspend fun getWorkersByCategory(category: ServiceCategory): UiState<List<Worker>> {
        return try {
            val snapshot = workersRef
                .whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
                .get().await()
            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
                .sortedByDescending { it.rating }
            UiState.Success(workers)
        } catch (e: Exception) {
            Log.e(TAG, "getWorkersByCategory failed for ${category.name}", e)
            UiState.Error(e.message ?: "Failed to load workers for ${category.displayName}")
        }
    }

    override suspend fun updateAvailability(uid: String, isAvailable: Boolean): UiState<Unit> {
        return try {
            workersRef.document(uid).update(FirestoreCollections.Fields.IS_AVAILABLE, isAvailable).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update availability")
        }
    }

    override suspend fun updateWorkerProfile(uid: String, updates: Map<String, Any>): UiState<Unit> {
        return try {
            workersRef.document(uid).update(updates).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun updateWorkerProfileFields(
        uid: String,
        name: String,
        phone: String,
        bio: String,
        role: String,
        category: ServiceCategory,
        baseLabourCharge: Int
    ): UiState<Unit> {
        return try {
            workersRef.document(uid).update(mapOf(
                "name" to name,
                "phone" to phone,
                "bio" to bio,
                "role" to role,
                "category" to category.name,
                "baseLabourCharge" to baseLabourCharge,
                "pricePerHour" to baseLabourCharge
            )).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update profile")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseWorker(doc: com.google.firebase.firestore.DocumentSnapshot): Worker? {
        if (!doc.exists()) return null
        return try {
            val data = doc.data ?: return null
            val categoryStr = data["category"] as? String ?: "OTHER"
            val category = try { ServiceCategory.valueOf(categoryStr) } catch (e: Exception) { ServiceCategory.OTHER }
            Worker(
                uid = data["uid"] as? String ?: doc.id,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                category = category,
                role = data["role"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                rating = (data["rating"] as? Double) ?: (data["rating"] as? Long)?.toDouble() ?: 0.0,
                reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 0,
                baseLabourCharge = (data["baseLabourCharge"] as? Number)?.toInt()
                    ?: (data["pricePerHour"] as? Number)?.toInt()
                    ?: 0,
                pricePerHour = (data["pricePerHour"] as? Number)?.toInt() ?: 0,
                distanceKm = (data["distanceKm"] as? Double) ?: (data["distanceKm"] as? Long)?.toDouble() ?: 0.0,
                isAvailable = data["isAvailable"] as? Boolean ?: true,
                isVerified = data["isVerified"] as? Boolean ?: false,
                isGovernmentCertified = data["isGovernmentCertified"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                avatarUrl = data["avatarUrl"] as? String ?: "",
                location = data["location"] as? String ?: "",
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse worker ${doc.id}", e)
            null
        }
    }
}
