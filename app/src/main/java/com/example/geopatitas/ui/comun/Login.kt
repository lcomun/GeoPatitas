package com.example.geopatitas.ui.comun

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a App Perritos", style = MaterialTheme.typography.headlineMedium)
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