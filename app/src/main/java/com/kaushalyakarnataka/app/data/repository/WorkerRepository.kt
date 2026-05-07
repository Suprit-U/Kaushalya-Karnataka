package com.kaushalyakarnataka.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
            val workers = snapshot.toObjects(Worker::class.java)
            UiState.Success(workers)
        } catch (e: Exception) {
            // Return sample data if Firestore not configured yet
            UiState.Success(sampleWorkers())
        }
    }

    override suspend fun getWorkerById(uid: String): UiState<Worker> {
        return try {
            val snapshot = workersRef.document(uid).get().await()
            val worker = snapshot.toObject(Worker::class.java)
                ?: return UiState.Error("Worker not found")
            UiState.Success(worker)
        } catch (e: Exception) {
            // Return sample worker for demo
            UiState.Success(sampleWorkers().first())
        }
    }

    override suspend fun searchWorkers(query: String, category: ServiceCategory?): UiState<List<Worker>> {
        return try {
            var ref: Query = workersRef
            if (category != null) {
                ref = ref.whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
            }
            val snapshot = ref.orderBy(FirestoreCollections.Fields.RATING, Query.Direction.DESCENDING).get().await()
            val workers = snapshot.toObjects(Worker::class.java)
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true) }
            UiState.Success(workers)
        } catch (e: Exception) {
            UiState.Success(sampleWorkers())
        }
    }

    override suspend fun getWorkersByCategory(category: ServiceCategory): UiState<List<Worker>> {
        return try {
            val snapshot = workersRef
                .whereEqualTo(FirestoreCollections.Fields.CATEGORY, category.name)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Worker::class.java))
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
}

/**
 * Sample workers seeded from the HTML prototype.
 * Used as fallback until Firebase is connected.
 */
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
        phone = "+91 98765 43210",
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
        phone = "+91 87654 32109",
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
        phone = "+91 76543 21098",
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
    ),
)
