package com.argesurec.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val currentUser: UserInfo? = null
)

class AuthViewModel(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                println("Auth Status: $status")
                _state.update { currentState ->
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            currentState.copy(
                                isLoggedIn = true,
                                currentUser = status.session.user,
                                isLoading = false
                            )
                        }
                        is SessionStatus.NotAuthenticated -> {
                            currentState.copy(
                                isLoggedIn = false,
                                currentUser = null,
                                isLoading = false
                            )
                        }
                        is SessionStatus.Initializing -> {
                            currentState.copy(isLoading = true)
                        }
                        else -> currentState
                    }
                }
            }
        }
    }

    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                val user = supabase.auth.currentUserOrNull()
                println("Auth: Sign-in successful. User: ${user?.id}")
                _state.update { it.copy(isLoading = false, isLoggedIn = user != null, currentUser = user) }
            } catch (e: Exception) {
                println("Auth Error (SignIn): ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message ?: "Giriş hatası oluştu.") }
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String, fullName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }
                val user = supabase.auth.currentUserOrNull()
                println("Auth: Sign-up successful. User: ${user?.id}")
                _state.update { it.copy(isLoading = false, isLoggedIn = user != null, currentUser = user) }
            } catch (e: Exception) {
                println("Auth Error (SignUp): ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message ?: "Kayıt hatası oluştu.") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                supabase.auth.signOut()
                _state.update { it.copy(isLoading = false, isLoggedIn = false, currentUser = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateProfile(fullName: String, department: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Update Auth Metadata
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }
                
                // 2. Update Public Profiles Table
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    supabase.from("profiles").update(
                        buildJsonObject {
                            put("full_name", fullName)
                            put("department", department ?: "")
                        }
                    ) {
                        filter { eq("id", userId) }
                    }
                }

                // Refresh current user
                val updatedUser = supabase.auth.currentUserOrNull()
                _state.update { it.copy(isLoading = false, currentUser = updatedUser) }
            } catch (e: Exception) {
                println("Auth Error (UpdateProfile): ${e.message}")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Profil güncelleme hatası.") }
            }
        }
    }

    fun checkSession() {
        // No longer needed as we observe sessionStatus in init
    }
}
