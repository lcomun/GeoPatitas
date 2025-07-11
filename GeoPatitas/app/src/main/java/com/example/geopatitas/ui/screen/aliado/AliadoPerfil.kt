package com.example.geopatitas.ui.screen.aliado

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geopatitas.ui.viewmodel.CurrentUserState
import com.example.geopatitas.ui.viewmodel.UserViewModel

@Composable
fun AliadoPerfilScreen(
    userViewModel: UserViewModel
) {
    val currentUserState by userViewModel.currentUserData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Perfil del Aliado",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (currentUserState) {
            is CurrentUserState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Cargando perfil...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is CurrentUserState.Success -> {
                val user = (currentUserState as CurrentUserState.Success).user

                //PerfilInfoItem("ID de usuario", user.idUsuario)
                InfoItem("Nombre", user.nombre)
                InfoItem("Correo", user.correo)
                //PerfilInfoItem("Tipo de usuario", user.tipoUsuario)
                //PerfilInfoItem("Casos atendidos", user.numCasosAtendidos.toString())
            }

            is CurrentUserState.Unauthenticated -> {
                Text(
                    "No hay sesión iniciada. Por favor, inicie sesión.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is CurrentUserState.Error -> {
                val errorMessage = (currentUserState as CurrentUserState.Error).message
                Text(
                    "Error al cargar el perfil: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                // Estado no reconocido
                Text("Estado desconocido del perfil.")
            }
        }
    }
}

@Composable
fun InfoItem(titulo: String, valor: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
