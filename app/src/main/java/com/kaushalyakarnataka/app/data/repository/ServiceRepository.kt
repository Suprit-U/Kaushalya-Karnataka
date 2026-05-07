package com.kaushalyakarnataka.app.data.repository

import android.util.Log
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

private const val TAG = "ServiceRepository"

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
            // Save as map to ensure proper field names
            val serviceMap = mapOf(
                "id" to newService.id,
                "workerId" to newService.workerId,
                "name" to newService.name,
                "category" to newService.category.name,
                "description" to newService.description,
                "startingPrice" to newService.startingPrice,
                "pricingType" to newService.pricingType.name,
                "estimatedDuration" to newService.estimatedDuration.name,
                "tags" to newService.tags,
                "isActive" to newService.isActive,
            )
            servicesRef.document(serviceId).set(serviceMap).await()
            Log.i(TAG, "Service saved: $serviceId for worker ${newService.workerId}")
            UiState.Success(newService)
        } catch (e: Exception) {
            Log.e(TAG, "addService failed", e)
            UiState.Error(e.message ?: "Failed to add service")
        }
    }

    override suspend fun updateService(service: Service): UiState<Unit> {
        return try {
            val serviceMap = mapOf(
                "id" to service.id,
                "workerId" to service.workerId,
                "name" to service.name,
                "category" to service.category.name,
                "description" to service.description,
                "startingPrice" to service.startingPrice,
                "pricingType" to service.pricingType.name,
                "estimatedDuration" to service.estimatedDuration.name,
                "tags" to service.tags,
                "isActive" to service.isActive,
            )
            servicesRef.document(service.id).set(serviceMap).await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update service")
        }
    }

    override suspend fun deleteService(serviceId: String): UiState<Unit> {
        return try {
            servicesRef.document(serviceId)
                .update("isActive", false)
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to delete service")
        }
    }

    override suspend fun getWorkerServices(workerId: String): UiState<List<Service>> {
        return try {
            val snapshot = servicesRef
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val services = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val categoryStr = data["category"] as? String ?: "OTHER"
                    val category = try { ServiceCategory.valueOf(categoryStr) } catch (e: Exception) { ServiceCategory.OTHER }
                    val pricingStr = data["pricingType"] as? String ?: "STARTING_AT"
                    val pricing = try { PricingType.valueOf(pricingStr) } catch (e: Exception) { PricingType.STARTING_AT }
                    val durationStr = data["estimatedDuration"] as? String ?: "ONE_HOUR"
                    val duration = try { ServiceDuration.valueOf(durationStr) } catch (e: Exception) { ServiceDuration.ONE_HOUR }
                    Service(
                        id = data["id"] as? String ?: doc.id,
                        workerId = data["workerId"] as? String ?: workerId,
                        name = data["name"] as? String ?: "",
                        category = category,
                        description = data["description"] as? String ?: "",
                        startingPrice = (data["startingPrice"] as? Long)?.toInt() ?: 0,
                        pricingType = pricing,
                        estimatedDuration = duration,
                        tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        isActive = data["isActive"] as? Boolean ?: true,
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse service ${doc.id}", e)
                    null
                }
            }
            if (services.isEmpty()) {
                Log.d(TAG, "No services found for worker $workerId")
            }
            UiState.Success(services)
        } catch (e: Exception) {
            Log.e(TAG, "getWorkerServices failed for $workerId", e)
            UiState.Success(emptyList())
        }
    }

    override suspend fun getWorkerPortfolio(workerId: String): UiState<List<PortfolioItem>> {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.WORKERS)
                .document(workerId)
                .collection("portfolio")
                .get()
                .await()
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val catStr = data["serviceCategory"] as? String ?: "OTHER"
                    val cat = try { ServiceCategory.valueOf(catStr) } catch (e: Exception) { ServiceCategory.OTHER }
                    PortfolioItem(
                        id = data["id"] as? String ?: doc.id,
                        workerId = data["workerId"] as? String ?: workerId,
                        photoUrl = data["photoUrl"] as? String ?: "",
                        caption = data["caption"] as? String ?: "",
                        serviceCategory = cat,
                    )
                } catch (e: Exception) {
                    null
                }
            }
            UiState.Success(items)
        } catch (e: Exception) {
            UiState.Success(emptyList())
        }
    }

    override suspend fun getPortfolioStats(workerId: String): UiState<PortfolioStats> {
        return try {
            val portfolio = (getWorkerPortfolio(workerId) as? UiState.Success)?.data ?: emptyList()
            UiState.Success(
                PortfolioStats(
                    projectCount = maxOf(portfolio.size, 0),
                    photoCount = portfolio.size,
                    averageRating = 4.5,
                )
            )
        } catch (e: Exception) {
            UiState.Success(PortfolioStats(0, 0, 0.0))
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
