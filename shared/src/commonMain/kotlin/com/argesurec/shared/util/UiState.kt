package com.argesurec.shared.util

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun <T> UiState<T>.getDataOrNull(): T? = (this as? UiState.Success)?.data
