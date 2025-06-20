package com.example.geopatitas.ui.ciudadano

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geopatitas.ui.theme.AppBackground

@Composable
fun CiudadanoPerfilScreen(navController: NavController) {

    AppBackground(imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg", overlayAlpha = 0.85f ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Perfil Ciudadano", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Nombre: Juan Pérez", style = MaterialTheme.typography.bodyLarge)
            Text("Correo: juanperez@example.com", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.navigate("login") }) {
                Text("Cerrar Sesión")
            }
        }

    }
}