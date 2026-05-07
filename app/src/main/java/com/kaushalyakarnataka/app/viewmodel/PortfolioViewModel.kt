package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.PortfolioItem
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.StorageRepository
import com.kaushalyakarnataka.app.utils.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UiState<PortfolioItem>?>(null)
    val uploadState: StateFlow<UiState<PortfolioItem>?> = _uploadState.asStateFlow()

    private val _portfolioItems = MutableStateFlow<UiState<List<PortfolioItem>>>(UiState.Loading)
    val portfolioItems: StateFlow<UiState<List<PortfolioItem>>> = _portfolioItems.asStateFlow()

    init {
        fetchPortfolioItems()
    }

    fun fetchPortfolioItems() {
        val workerId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _portfolioItems.value = UiState.Loading
            try {
                val snapshot = firestore.collection(FirestoreCollections.WORKERS)
                    .document(workerId)
                    .collection("portfolio")
                    .get()
                    .await()
                val items = snapshot.toObjects(PortfolioItem::class.java)
                _portfolioItems.value = UiState.Success(items)
            } catch (e: Exception) {
                _portfolioItems.value = UiState.Error("Failed to fetch portfolio items: ${e.message}")
            }
        }
    }

    fun uploadPortfolioItem(imageBytes: ByteArray, caption: String, category: ServiceCategory) {
        val workerId = authRepository.currentUser?.uid ?: return

        _uploadState.value = UiState.Loading
        viewModelScope.launch {
            // 1. Upload image to Supabase Storage
            val uploadResult = storageRepository.uploadImage(imageBytes, "portfolio/$workerId")
            
            if (uploadResult is UiState.Success) {
                // 2. Save portfolio entry to Firestore
                val publicUrl = uploadResult.data
                val itemId = UUID.randomUUID().toString()
                
                val item = PortfolioItem(
                    id = itemId,
                    workerId = workerId,
                    photoUrl = publicUrl,
                    caption = caption,
                    serviceCategory = category
                )
                
                try {
                    firestore.collection(FirestoreCollections.WORKERS)
                        .document(workerId)
                        .collection("portfolio")
                        .document(itemId)
                        .set(item)
                        .await()
                    
                    _uploadState.value = UiState.Success(item)
                    fetchPortfolioItems() // Refresh the list
                } catch (e: Exception) {
                    _uploadState.value = UiState.Error("Failed to save portfolio metadata")
                }
            } else if (uploadResult is UiState.Error) {
                _uploadState.value = UiState.Error(uploadResult.message)
            }
        }
    }

    fun deletePortfolioItem(itemId: String) {
        val workerId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.WORKERS)
                    .document(workerId)
                    .collection("portfolio")
                    .document(itemId)
                    .delete()
                    .await()
                fetchPortfolioItems() // Refresh list
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    fun clearState() {
        _uploadState.value = null
    }
}
