package com.example.geopatitas.ui.comun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geopatitas.ui.theme.AppBackground

@Composable
fun LoginScreen(navController: NavController) {

    AppBackground(imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg", overlayAlpha = 0.85f ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido a GeoPatitas", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate("dashboard_ciudadano") }) {
                Text("Entrar como Ciudadano")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("dashboard_ong") }) {
                Text("Entrar como ONG")
            }
        }
    }

}