package com.kaushalyakarnataka.app.data.repository

import com.kaushalyakarnataka.app.utils.UiState
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

interface StorageRepository {
    suspend fun uploadImage(byteArray: ByteArray, path: String): UiState<String>
    suspend fun deleteImage(path: String): UiState<Unit>
}

class StorageRepositoryImpl @Inject constructor(
    private val supabaseStorage: Storage
) : StorageRepository {

    private val bucketId = "kaushalya-storage"
    private val bucket get() = supabaseStorage.from(bucketId)

    override suspend fun uploadImage(byteArray: ByteArray, path: String): UiState<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val fullPath = "$path/$fileName"
            
            bucket.upload(fullPath, byteArray) {
                upsert = true
            }
            
            // Get public URL
            val publicUrl = bucket.publicUrl(fullPath)
            UiState.Success(publicUrl)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to upload image")
        }
    }

    override suspend fun deleteImage(path: String): UiState<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            bucket.delete(listOf(path))
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to delete image")
        }
    }
}
