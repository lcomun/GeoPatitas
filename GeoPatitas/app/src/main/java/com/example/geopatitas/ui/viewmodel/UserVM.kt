package com.example.geopatitas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.data.Usuario
import com.example.geopatitas.data.repository.AuthRepository
import com.example.geopatitas.data.repository.AuthRepository2
import com.example.geopatitas.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CurrentUserState2 {
    object Loading : CurrentUserState()
    data class Success(val user: Usuario) : CurrentUserState()
    data class Error(val message: String) : CurrentUserState()
    object Unauthenticated : CurrentUserState()
}

class UserVM(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository2
) : ViewModel() {

    private val _currentUserData = MutableStateFlow<CurrentUserState>(CurrentUserState.Unauthenticated)
    val currentUserData: StateFlow<CurrentUserState> = _currentUserData.asStateFlow()

    // Carga usuario desde Firestore dado un email o id (lo que uses para identificar)
    fun loadUserData(userEmail: String) {
        _currentUserData.value = CurrentUserState.Loading
        viewModelScope.launch {
            try {
                val usuarioFirestore = userRepository.getRegisteredUser(userEmail)
                if (usuarioFirestore != null) {
                    _currentUserData.value = CurrentUserState.Success(usuarioFirestore)
                } else {
                    _currentUserData.value = CurrentUserState.Error("Usuario no registrado en la app")
                }
            } catch (e: Exception) {
                _currentUserData.value = CurrentUserState.Error("Error al cargar usuario: ${e.message}")
            }
        }
    }

    // Para refrescar los datos
    fun refreshUserData(userEmail: String) {
        loadUserData(userEmail)
    }

    // Limpia datos, por ejemplo al cerrar sesión
    fun clearUserData() {
        _currentUserData.value = CurrentUserState.Unauthenticated
    }

    fun LogOut(){
        authRepository.cerrarSesion()
    }
}
