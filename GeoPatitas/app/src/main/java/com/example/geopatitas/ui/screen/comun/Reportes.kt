package com.example.geopatitas.ui.screen.comun

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.geopatitas.utils.LocationUtils
import com.example.geopatitas.ui.viewmodel.CurrentUserState
import com.example.geopatitas.ui.viewmodel.LocationViewModel
import com.example.geopatitas.ui.viewmodel.ReporteListViewModel2
import com.example.geopatitas.ui.viewmodel.UserViewModel

@Composable
fun ReportesScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    reporteListViewModel: ReporteListViewModel2,
    locationVM: LocationViewModel,
    onReporteClick: (String) -> Unit
) {
    val context = LocalContext.current

    // ViewModels
    val currentUserState = userViewModel.currentUserData.collectAsState().value

    // UI state
    var selectedTab by remember { mutableStateOf(0) }

    // Permiso de ubicación
    var permisoConcedido by remember { mutableStateOf(false) }

    // Pedir permiso con launcher
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permisoConcedido = granted
        if (granted) {
            LocationUtils.startLocationUpdates { location ->
                locationVM.setUbicacion(location)
            }
        }
    }

    // Verificar si ya tiene permiso al entrar
    LaunchedEffect(Unit) {
        LocationUtils.initialize(context)
        reporteListViewModel.cargarReportes()
        val tienePermiso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        permisoConcedido = tienePermiso

        if (tienePermiso) {
            LocationUtils.startLocationUpdates { location ->
                locationVM.setUbicacion(location)
            }
        }
    }

    // Detener actualizaciones al salir
    DisposableEffect(Unit) {
        onDispose {
            locationVM.detenerActualizaciones()
        }
    }

    // UI Layout
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Mapa") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Álbum") }
            )
        }

        when (selectedTab) {
            0 -> {
                // Mostrar el mapa dentro de esta misma pantalla
                MapaReporteScreen(
                    navController = navController,
                    locationVM = locationVM,
                    reporteListViewModel = reporteListViewModel,
                    onReporteClick = onReporteClick
                )
            }
            1 -> {
                AlbumReporteScreen(
                    locationVM = locationVM,
                    reporteListViewModel = reporteListViewModel,
                    soloDelUsuario = false,
                    onReporteClick = onReporteClick
                )
            }
        }
    }

    if(!permisoConcedido){
        // Botón para solicitar ubicación
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Dar ubicación")
            }
        }
    }

    // Botón de "Crear reporte" solo para ciudadanos
    if (currentUserState is CurrentUserState.Success &&
        currentUserState.user.tipoUsuario == "Vecino"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { navController.navigate("reportar") },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text("Crear reporte")
            }
        }
    }
}
