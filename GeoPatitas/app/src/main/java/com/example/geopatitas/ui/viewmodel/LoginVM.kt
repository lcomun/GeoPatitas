package com.example.geopatitas.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.data.repository.AuthRepository2
import com.example.geopatitas.data.repository.UserRepository
import com.example.geopatitas.utils.AuthRes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


/*

sealed class LoginUiState {
    object Idle : LoginUiState() // Estado inicial o cuando no hay operación en curso
    object Loading : LoginUiState() // Operación en curso (ej. autenticando)
    data class Error(val message: String) : LoginUiState() // Error durante la operación

    // Estados específicos para el flujo de Google Sign-In
    object StartGoogleSignInFlow : LoginUiState() // Indicar a la UI que lance el Intent de Google
    data class GoogleAccountReceived(val account: GoogleSignInAccount) : LoginUiState() // Cuenta de Google obtenida

    // Estados de éxito después de la autenticación completa
    data class AuthenticatedAndRegistered(val user: Usuario) : LoginUiState() // Usuario autenticado y ya registrado
    data class AuthenticatedAndNeedsRegistration(val name: String?, val email: String) : LoginUiState() // Autenticado, pero necesita registrarse en tu BD
}

 */

class LoginVM(
    private val authRepository: AuthRepository2,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Ya no necesitas GoogleSignInClient aquí, se maneja en el repo
    fun iniciarGoogleSignIn(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        _uiState.value = LoginUiState.Loading
        authRepository.iniciarFlujoGoogle(launcher)
    }

    fun procesarResultadoGoogle(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                when (val result = authRepository.procesarResultadoGoogle(task)) {
                    is AuthRes.Success -> {
                        val account = result.data
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                        _uiState.value = LoginUiState.Loading

                        // Autenticar en Firebase
                        when (val authResult = authRepository.iniciarSesionConCredencialesGoogle(credential)) {
                            is AuthRes.Success -> {
                                val firebaseUser = authResult.data
                                val email = firebaseUser.email ?: ""

                                if (email.isEmpty()) {
                                    _uiState.value = LoginUiState.Error("Correo electrónico nulo en Firebase")
                                    return@launch
                                }

                                // Verificar si el usuario ya está registrado
                                val usuarioRegistrado = userRepository.getRegisteredUser(email)
                                if (usuarioRegistrado != null) {
                                    _uiState.value = LoginUiState.AuthenticatedAndRegistered(usuarioRegistrado)
                                } else {
                                    _uiState.value = LoginUiState.AuthenticatedAndNeedsRegistration(
                                        firebaseUser.displayName,
                                        email
                                    )
                                }
                            }

                            is AuthRes.Error -> {
                                _uiState.value = LoginUiState.Error("Error autenticando con Firebase: ${authResult.errorMessage}")
                            }
                        }
                    }

                    is AuthRes.Error -> {
                        _uiState.value = LoginUiState.Error("Error Google Sign-In: ${result.errorMessage}")
                    }
                }
            } catch (e: ApiException) {
                _uiState.value = LoginUiState.Error("Error Google Sign-In: ${e.message ?: "desconocido"}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.cerrarSesion()
                _uiState.value = LoginUiState.Idle
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error al cerrar sesión: ${e.message}")
            }
        }
    }
}
