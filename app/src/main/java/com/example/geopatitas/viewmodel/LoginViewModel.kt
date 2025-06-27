package com.example.geopatitas.viewmodel


import android.util.Log
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.R
import com.example.geopatitas.model.Usuario
import com.example.geopatitas.repository.AuthRepository
import com.example.geopatitas.repository.UserRepository
import com.example.geopatitas.utils.AuthRes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define los posibles estados de la UI de tu pantalla de Login
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

class LoginViewModel(
    private val authRepository: AuthRepository, // Inyecta el repositorio de autenticación
    private val userRepository: UserRepository, // Inyecta el repositorio de usuarios (para verificar registro)
    private val applicationContext: Context // Necesitas Context para GoogleSignInClient
) : ViewModel() {

    // MutableStateFlow para el estado de la UI que la pantalla observará
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Cliente de Google Sign-In, inicializado con el Contexto de la aplicación
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(applicationContext, gso)
    }

    /**
     * Llamado cuando el botón "Iniciar sesión con Google" es presionado.
     * Cambia el estado para indicar a la UI que inicie el flujo de Google.
     */
    //LOGIN PASO 2
    fun onGoogleSignInClicked() {
        Log.d("LoginViewModel", "onGoogleSignInClicked: Iniciando flujo de Google Sign-In")
        _uiState.value = LoginUiState.StartGoogleSignInFlow
    }

    /**
     * Procesa el resultado de la ActivityResultLauncher de Google Sign-In.
     * Maneja el éxito o el error inicial de Google.
     */
    //LOGIN PASO 3
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("LoginViewModel", "handleGoogleSignInResult: Google Sign-In exitoso. Cuenta: ${account.displayName}")
                _uiState.value = LoginUiState.GoogleAccountReceived(account)
            } catch (e: ApiException) {
                Log.e("LoginViewModel", "handleGoogleSignInResult: Error al obtener la cuenta de Google. Mensaje: ${e.message}")
                _uiState.value = LoginUiState.Error(e.message ?: "Fallo el inicio de sesión de Google.")
            }
        }
    }


    /**
     * Autentica al usuario con Firebase usando las credenciales de Google
     * y luego verifica si el usuario ya está registrado en tu base de datos.
     */
    //LOGIN PASO 4
    fun authenticateWithFirebaseAndCheckRegistration(googleAccount: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
            Log.d("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Iniciando sesión con Firebase para ${googleAccount.email}")

            when (val authRes = authRepository.signInWithGoogleCredential(credential)) {
                is AuthRes.Success -> {
                    val firebaseUser = authRes.data
                    Log.d("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Autenticación Firebase exitosa para ${firebaseUser.email}")

                    val userEmail = firebaseUser.email ?: ""
                    val userName = firebaseUser.displayName ?: ""

                    if (userEmail.isNotEmpty()) {
                        // ¡Aquí es donde usas el UserRepository!
                        val usuarioRegistrado = userRepository.getRegisteredUser(userEmail)
                        if (usuarioRegistrado != null) {
                            Log.d("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Usuario encontrado en la base de datos.")
                            _uiState.value = LoginUiState.AuthenticatedAndRegistered(usuarioRegistrado)
                        } else {
                            Log.d("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Usuario no registrado. Requiere registro.")
                            _uiState.value = LoginUiState.AuthenticatedAndNeedsRegistration(userName, userEmail)
                        }
                    } else {
                        Log.e("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Correo electrónico de Firebase es nulo.")
                        _uiState.value = LoginUiState.Error("Correo electrónico de usuario Firebase nulo.")
                    }
                }
                is AuthRes.Error -> {
                    Log.e("LoginViewModel", "authenticateWithFirebaseAndCheckRegistration: Error al autenticar con Firebase. ${authRes.errorMessage}")
                    _uiState.value = LoginUiState.Error("Error al iniciar sesión con Firebase: ${authRes.errorMessage}")
                }
            }
        }
    }


    /**
     * Expone el Intent necesario para lanzar el flujo de Google Sign-In.
     */
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /*
    fun signOut() {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is AuthRes.Success -> {
                    Log.d("LoginViewModel", "signOut: Sesión cerrada correctamente.")
                    _uiState.value = LoginUiState.Idle // O un estado que indique "sesión cerrada"
                }
                is AuthRes.Error -> {
                    Log.e("LoginViewModel", "signOut: Error al cerrar sesión. ${result.errorMessage}")
                    _uiState.value = LoginUiState.Error("Error al cerrar sesión: ${result.errorMessage}")
                }
            }
        }
    }
     */

    fun signOut() {
        viewModelScope.launch {
            // Primero, cierra la sesión en Firebase
            when (val result = authRepository.signOut()) {
                is AuthRes.Success -> {
                    Log.d("LoginViewModel", "signOut: Sesión cerrada correctamente en Firebase.")
                    // Ahora, cierra la sesión en Google
                    googleSignInClient.signOut().addOnCompleteListener {
                        Log.d("LoginViewModel", "Google Sign-Out completo.")
                        _uiState.value = LoginUiState.Idle // Restablece el estado a "Idle" cuando se cierra sesión
                    }
                }
                is AuthRes.Error -> {
                    Log.e("LoginViewModel", "signOut: Error al cerrar sesión en Firebase. ${result.errorMessage}")
                    _uiState.value = LoginUiState.Error("Error al cerrar sesión: ${result.errorMessage}")
                }
            }
        }
    }


}