package com.example.geopatitas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.model.Usuario // Asegúrate de la ruta correcta a tu data class
import com.example.geopatitas.repository.AuthRepository
import com.example.geopatitas.repository.UserRepository
import com.example.geopatitas.utils.obtenerUsuarioDesdeFirestore // Tu función suspendida
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed class para representar los diferentes estados de carga del usuario
sealed class CurrentUserState {
    object Loading : CurrentUserState() // Indicando que se está cargando
    data class Success(val user: Usuario) : CurrentUserState() // Usuario cargado exitosamente
    data class Error(val message: String) : CurrentUserState() // Error al cargar
    object Unauthenticated : CurrentUserState() // No hay usuario logueado o email nulo
}

// Inyecta los repositorios necesarios en el constructor del ViewModel
// En un proyecto real, usarías Hilt/Koin aquí con @HiltViewModel y @Inject
class UserViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // MutableStateFlow para que el estado pueda ser modificado internamente
    private val _currentUserData = MutableStateFlow<CurrentUserState>(CurrentUserState.Loading)
    // StateFlow público para que los Composable lo observen de forma segura
    val currentUserData: StateFlow<CurrentUserState> = _currentUserData.asStateFlow()

    init {
        // Al inicializar el ViewModel, intentamos cargar los datos del usuario
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            // Delega la obtención del usuario de Firebase al AuthRepository
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                val userEmail = firebaseUser.email
                if (userEmail != null) {
                    try {
                        // Delega la obtención del usuario de Firestore al UserRepository
                        val usuarioFirestore = userRepository.getRegisteredUser(userEmail)
                        if (usuarioFirestore != null) {
                            _currentUserData.value = CurrentUserState.Success(usuarioFirestore)
                        } else {
                            // Si el usuario está autenticado en Firebase pero no en Firestore (necesita registro)
                            _currentUserData.value = CurrentUserState.Error("Usuario autenticado pero no registrado en la aplicación. (Puede necesitar registro)")
                        }
                    } catch (e: Exception) {
                        // Manejo de errores al consultar UserRepository
                        _currentUserData.value = CurrentUserState.Error("Error al cargar datos del usuario: ${e.message}")
                    }
                } else {
                    // Usuario de Firebase sin email (ej. solo por teléfono)
                    _currentUserData.value = CurrentUserState.Unauthenticated
                }
            } else {
                // No hay usuario de Firebase logueado
                _currentUserData.value = CurrentUserState.Unauthenticated
            }
        }
    }

    // Opcional: Función para refrescar los datos del usuario si se actualizan en Firestore
    fun refreshUserData() {
        loadUserData() // Simplemente vuelve a cargar los datos
    }

    // Opcional: Limpiar los datos del usuario si cierra sesión
    fun clearUserData() {
        _currentUserData.value = CurrentUserState.Unauthenticated
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Llamamos al método de logout del AuthRepository
                authRepository.signOut()

                // Limpiamos los datos del usuario
                _currentUserData.value = CurrentUserState.Unauthenticated
            } catch (e: Exception) {
                // Si hay algún error durante el cierre de sesión, manejamos el error
                _currentUserData.value = CurrentUserState.Error("Error al cerrar sesión: ${e.message}")
            }
        }
    }

}