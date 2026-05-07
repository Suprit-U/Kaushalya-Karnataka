package com.kaushalyakarnataka.app.utils

/**
 * A sealed class representing the state of UI data.
 * Used by ViewModels to communicate loading, success, and error states to the Compose UI.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
