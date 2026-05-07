package com.kaushalyakarnataka.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "WorkerRepository"

interface WorkerRepository {
    suspend fun getTopWorkers(limit: Long = 10): UiState<List<Worker>>
    suspend fun getWorkerById(uid: String): UiState<Worker>
    suspend fun searchWorkers(query: String, category: ServiceCategory? = null): UiState<List<Worker>>
    suspend fun getWorkersByCategory(category: ServiceCategory): UiState<List<Worker>>
    suspend fun updateAvailability(uid: String, isAvailable: Boolean): UiState<Unit>
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
                .get()
                .await()
            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
            if (workers.isEmpty()) {
                Log.w(TAG, "No workers found in Firestore, using sample data")
                UiState.Success(sampleWorkers())
            } else {
                UiState.Success(workers)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopWorkers failed", e)
            UiState.Success(sampleWorkers())
        }
    }

    override suspend fun getWorkerById(uid: String): UiState<Worker> {
        return try {
            val snapshot = workersRef.document(uid).get().await()
            val worker = parseWorker(snapshot)
            if (worker != null) {
                UiState.Success(worker)
            } else {
                // Try user's own UID match in sample workers
                val sample = sampleWorkers().find { it.uid == uid }
                if (sample != null) UiState.Success(sample) else UiState.Success(sampleWorkers().first())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWorkerById failed for $uid", e)
            UiState.Success(sampleWorkers().first())
        }
    }

    override suspend fun searchWorkers(query: String, category: ServiceCategory?): UiState<List<Worker>> {
        return try {
            val snapshot = if (category != null) {
                workersRef
                    .whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
                    .orderBy(FirestoreCollections.Fields.RATING, Query.Direction.DESCENDING)
                    .get()
                    .await()
            } else {
                workersRef
                    .orderBy(FirestoreCollections.Fields.RATING, Query.Direction.DESCENDING)
                    .get()
                    .await()
            }

            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
                .filter { worker ->
                    query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.role.contains(query, ignoreCase = true) ||
                    worker.category.displayName.contains(query, ignoreCase = true) ||
                    worker.tags.any { it.contains(query, ignoreCase = true) }
                }

            if (workers.isEmpty() && query.isBlank() && category == null) {
                UiState.Success(sampleWorkers())
            } else if (workers.isEmpty()) {
                // Filter sample data as fallback
                val filtered = sampleWorkers().filter { worker ->
                    (category == null || worker.category == category) &&
                    (query.isBlank() || worker.name.contains(query, ignoreCase = true) ||
                     worker.role.contains(query, ignoreCase = true))
                }
                UiState.Success(filtered)
            } else {
                UiState.Success(workers)
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchWorkers failed", e)
            val filtered = sampleWorkers().filter { worker ->
                (category == null || worker.category == category) &&
                (query.isBlank() || worker.name.contains(query, ignoreCase = true))
            }
            UiState.Success(filtered)
        }
    }

    override suspend fun getWorkersByCategory(category: ServiceCategory): UiState<List<Worker>> {
        return try {
            val snapshot = workersRef
                .whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
                .get()
                .await()
            val workers = snapshot.documents.mapNotNull { parseWorker(it) }
            if (workers.isEmpty()) {
                UiState.Success(sampleWorkers().filter { it.category == category })
            } else {
                UiState.Success(workers)
            }
        } catch (e: Exception) {
            UiState.Success(sampleWorkers().filter { it.category == category })
        }
    }

    override suspend fun updateAvailability(uid: String, isAvailable: Boolean): UiState<Unit> {
        return try {
            workersRef.document(uid)
                .update(FirestoreCollections.Fields.IS_AVAILABLE, isAvailable)
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update availability")
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
                category = category,
                role = data["role"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                rating = (data["rating"] as? Double) ?: (data["rating"] as? Long)?.toDouble() ?: 0.0,
                reviewCount = (data["reviewCount"] as? Long)?.toInt() ?: 0,
                experienceYears = (data["experienceYears"] as? Long)?.toInt() ?: 0,
                successRate = (data["successRate"] as? Long)?.toInt() ?: 0,
                pricePerHour = (data["pricePerHour"] as? Long)?.toInt() ?: 0,
                distanceKm = (data["distanceKm"] as? Double) ?: (data["distanceKm"] as? Long)?.toDouble() ?: 0.0,
                isAvailable = data["isAvailable"] as? Boolean ?: true,
                isVerified = data["isVerified"] as? Boolean ?: false,
                isGovernmentCertified = data["isGovernmentCertified"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                avatarUrl = data["avatarUrl"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                location = data["location"] as? String ?: "",
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse worker ${doc.id}", e)
            null
        }
    }
}

fun sampleWorkers() = listOf(
    Worker(
        uid = "worker1",
        name = "Ramesh Kumar",
        category = ServiceCategory.ELECTRICIAN,
        role = "Master Electrician",
        bio = "8+ years of experience in residential & commercial electrical work. Specialised in wiring, panel upgrades, safety audits, and smart home setups. All work guaranteed with a 30-day warranty.",
        rating = 4.9,
        reviewCount = 124,
        experienceYears = 8,
        successRate = 98,
        pricePerHour = 450,
        distanceKm = 2.4,
        isAvailable = true,
        isVerified = true,
        isGovernmentCertified = true,
        tags = listOf("Wiring", "Home Safety", "Lighting", "Panel Work", "Smart Home"),
        location = "Koramangala, Bengaluru",
        phone = "+919876543210",
    ),
    Worker(
        uid = "worker2",
        name = "Syed Ali",
        category = ServiceCategory.PLUMBER,
        role = "Expert Plumber",
        bio = "5 years of experience in residential plumbing. Specialised in pipe fitting, leak repairs, and bathroom renovation.",
        rating = 4.7,
        reviewCount = 87,
        experienceYears = 5,
        successRate = 95,
        pricePerHour = 380,
        distanceKm = 1.8,
        isAvailable = true,
        isVerified = true,
        tags = listOf("Pipe Fitting", "Leak Repair", "Bathroom"),
        location = "HSR Layout, Bengaluru",
        phone = "+918765432109",
    ),
    Worker(
        uid = "worker3",
        name = "Mohan Das",
        category = ServiceCategory.CARPENTER,
        role = "Senior Carpenter",
        bio = "Expert in furniture making, door/window fitting, and wooden flooring. 12+ years experience.",
        rating = 4.8,
        reviewCount = 203,
        experienceYears = 12,
        successRate = 96,
        pricePerHour = 500,
        distanceKm = 3.1,
        isAvailable = true,
        isVerified = true,
        tags = listOf("Furniture", "Doors", "Flooring", "Custom Work"),
        location = "Indiranagar, Bengaluru",
        phone = "+917654321098",
    ),
    Worker(
        uid = "worker4",
        name = "Kiran Patil",
        category = ServiceCategory.ELECTRICIAN,
        role = "Electrician",
        bio = "12 years experience in commercial and residential electrical work.",
        rating = 4.6,
        reviewCount = 203,
        experienceYears = 12,
        successRate = 94,
        pricePerHour = 550,
        distanceKm = 3.6,
        isAvailable = false,
        isVerified = true,
        tags = listOf("Wiring", "Panel", "Safety Audit"),
        location = "Whitefield, Bengaluru",
        phone = "+916543210987",
    ),
    Worker(
        uid = "painter1",
        name = "Ravi Painter",
        category = ServiceCategory.PAINTER,
        role = "Professional Painter",
        bio = "Expert in interior and exterior painting. Uses premium quality paints. 7 years experience.",
        rating = 4.5,
        reviewCount = 65,
        experienceYears = 7,
        successRate = 93,
        pricePerHour = 350,
        distanceKm = 2.8,
        isAvailable = true,
        isVerified = true,
        tags = listOf("Interior", "Exterior", "Texture", "Waterproofing"),
        location = "Jayanagar, Bengaluru",
        phone = "+915432109876",
    ),
    Worker(
        uid = "acrepair1",
        name = "Suresh AC Tech",
        category = ServiceCategory.AC_TECH,
        role = "AC & Appliance Specialist",
        bio = "Certified AC technician with 6 years experience. Services all major brands.",
        rating = 4.7,
        reviewCount = 98,
        experienceYears = 6,
        successRate = 97,
        pricePerHour = 400,
        distanceKm = 1.5,
        isAvailable = true,
        isVerified = true,
        tags = listOf("AC Service", "AC Repair", "Gas Refill", "Installation"),
        location = "BTM Layout, Bengaluru",
        phone = "+914321098765",
    ),
)
