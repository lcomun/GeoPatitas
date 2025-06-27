package com.example.geopatitas.ui.vecino

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geopatitas.viewmodel.CurrentUserState
import com.example.geopatitas.viewmodel.UserViewModel

@Composable
fun VecinoPerfilScreen(
    userViewModel: UserViewModel // ¡Solo necesita el UserViewModel directamente!
) {
    // El resto de la lógica es la misma: observar y renderizar
    // No necesitamos obtener el NavBackStackEntry ni usar la función reusable aquí.
    val currentUserState by userViewModel.currentUserData.collectAsState()

    // Logging (LaunchedEffect)
    LaunchedEffect(currentUserState) {
        when (currentUserState) {
            is CurrentUserState.Loading -> Log.d("PerfilScreen", "Loading user profile data...")
            is CurrentUserState.Success -> {
                val user = (currentUserState as CurrentUserState.Success).user
                Log.d("PerfilScreen", "User profile loaded: ${user.nombre}")
            }
            is CurrentUserState.Unauthenticated -> Log.d("PerfilScreen", "User is unauthenticated on profile screen.")
            is CurrentUserState.Error -> Log.e("PerfilScreen", "Error loading user profile: ${(currentUserState as CurrentUserState.Error).message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil Ciudadano", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        when (currentUserState) {
            is CurrentUserState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando perfil...", style = MaterialTheme.typography.bodyLarge)
            }
            is CurrentUserState.Success -> {
                val user = (currentUserState as CurrentUserState.Success).user
                // Asegúrate que el nombre de la propiedad sea correcto (idUsuario o id)
                Text("ID: ${user.idUsuario}", style = MaterialTheme.typography.bodyLarge)
                Text("Nombre: ${user.nombre}", style = MaterialTheme.typography.bodyLarge)
                Text("Correo: ${user.correo}", style = MaterialTheme.typography.bodyLarge)
                Text("Tipo de Usuario: ${user.tipoUsuario}", style = MaterialTheme.typography.bodyLarge)
            }
            is CurrentUserState.Unauthenticated -> {
                Text("No hay sesión iniciada. Por favor, inicie sesión.", style = MaterialTheme.typography.bodyLarge)
            }
            is CurrentUserState.Error -> {
                val errorMessage = (currentUserState as CurrentUserState.Error).message
                Text("Error al cargar el perfil: $errorMessage", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}