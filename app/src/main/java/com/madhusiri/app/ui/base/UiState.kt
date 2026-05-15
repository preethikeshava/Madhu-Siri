package com.madhusiri.app.ui.base

/**
 * A generic class that holds a value with its loading status.
 * Used for Unidirectional Data Flow (UDF) state management across all ViewModels.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String? = null) : UiState<Nothing>()
}

/**
 * Extension function to easily map data.
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(exception, message)
        UiState.Loading -> UiState.Loading
    }
}
