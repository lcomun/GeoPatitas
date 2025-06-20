package com.example.geopatitas.ui.ong

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.geopatitas.ui.theme.AppBackground

@Composable
fun OrganizacionMapaScreen(navController: NavController) {

    AppBackground(imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg", overlayAlpha = 0.85f ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pantalla Mapa")
        }

    }
}