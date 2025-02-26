// data/state/AuthState.kt
package com.ran.refeed.data.state

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}