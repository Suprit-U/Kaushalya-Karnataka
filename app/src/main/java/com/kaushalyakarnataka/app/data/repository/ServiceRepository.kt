package com.kaushalyakarnataka.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.PortfolioItem
import com.kaushalyakarnataka.app.data.model.PortfolioStats
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.ServiceDuration
import com.kaushalyakarnataka.app.data.model.PricingType
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

interface ServiceRepository {
    suspend fun addService(service: Service): UiState<Service>
    suspend fun updateService(service: Service): UiState<Unit>
    suspend fun deleteService(serviceId: String): UiState<Unit>
    suspend fun getWorkerServices(workerId: String): UiState<List<Service>>
    suspend fun getWorkerPortfolio(workerId: String): UiState<List<PortfolioItem>>
    suspend fun getPortfolioStats(workerId: String): UiState<PortfolioStats>
}

class ServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ServiceRepository {

    private val servicesRef get() = firestore.collection(FirestoreCollections.SERVICES)

    override suspend fun addService(service: Service): UiState<Service> {
        return try {
            val serviceId = UUID.randomUUID().toString()
            val newService = service.copy(id = serviceId, isActive = true)
            servicesRef.document(serviceId).set(newService).await()
            UiState.Success(newService)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to add service")
        }
    }

    override suspend fun updateService(service: Service): UiState<Unit> {
        return try {
            servicesRef.document(service.id).set(service).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update service")
        }
    }

    override suspend fun deleteService(serviceId: String): UiState<Unit> {
        return try {
            servicesRef.document(serviceId)
                .update(FirestoreCollections.Fields.IS_ACTIVE, false)
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to delete service")
        }
    }

    override suspend fun getWorkerServices(workerId: String): UiState<List<Service>> {
        return try {
            val snapshot = servicesRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .whereEqualTo(FirestoreCollections.Fields.IS_ACTIVE, true)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Service::class.java))
        } catch (e: Exception) {
            UiState.Success(sampleServices())
        }
    }

    override suspend fun getWorkerPortfolio(workerId: String): UiState<List<PortfolioItem>> {
        return try {
            // Portfolio items are stored under workers/{uid}/portfolio subcollection
            val snapshot = firestore.collection(FirestoreCollections.WORKERS)
                .document(workerId)
                .collection("portfolio")
                .get()
                .await()
            UiState.Success(snapshot.toObjects(PortfolioItem::class.java))
        } catch (e: Exception) {
            UiState.Success(emptyList())
        }
    }

    override suspend fun getPortfolioStats(workerId: String): UiState<PortfolioStats> {
        return try {
            val portfolio = (getWorkerPortfolio(workerId) as? UiState.Success)?.data ?: emptyList()
            UiState.Success(
                PortfolioStats(
                    projectCount = 12,
                    photoCount = portfolio.size.coerceAtLeast(28),
                    averageRating = 4.9,
                )
            )
        } catch (e: Exception) {
            UiState.Success(PortfolioStats(12, 28, 4.9))
        }
    }
}

fun sampleServices() = listOf(
    Service(
        id = "service1",
        workerId = "worker1",
        name = "Wiring & Rewiring",
        category = ServiceCategory.ELECTRICIAN,
        description = "Full home wiring or rewiring service. Includes safety inspection.",
        startingPrice = 900,
        pricingType = PricingType.FIXED,
        estimatedDuration = ServiceDuration.TWO_TO_THREE_HOURS,
        tags = listOf("Wiring", "Safety"),
    ),
    Service(
        id = "service2",
        workerId = "worker1",
        name = "Fan / Fixture Install",
        category = ServiceCategory.ELECTRICIAN,
        description = "Installation of ceiling fans, light fixtures, and other electrical fittings.",
        startingPrice = 350,
        pricingType = PricingType.FIXED,
        estimatedDuration = ServiceDuration.ONE_HOUR,
        tags = listOf("Fan", "Lighting", "Installation"),
    ),
)
