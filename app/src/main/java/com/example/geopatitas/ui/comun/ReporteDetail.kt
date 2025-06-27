package com.example.geopatitas.ui.comun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.geopatitas.viewmodel.CurrentUserState
import com.example.geopatitas.viewmodel.ReporteListViewModel2
import com.example.geopatitas.viewmodel.UserViewModel

@Composable
fun ReporteDetailScreen(navController: NavController, reporteId: String, userViewModel: UserViewModel, origen: String) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentUserState = userViewModel.currentUserData.collectAsState().value

    // Usa currentBackStackEntry como key para remember
    val parentEntry = remember(currentBackStackEntry) {
        navController.getBackStackEntry("reportes")
    }

    val reporteListViewModel: ReporteListViewModel2 = viewModel(parentEntry)

    val currentUser = userViewModel.currentUserData.collectAsState().value
    val isOrganizacion = (currentUser as? CurrentUserState.Success)?.user?.tipoUsuario == "Organizacion"

    LaunchedEffect(Unit) {
        if (reporteListViewModel.listaReportes.value.isEmpty()) {
            reporteListViewModel.cargarReportes()
        }
        reporteListViewModel.cargarReportePorId(reporteId)
    }

    val reporte = reporteListViewModel.reporteSeleccionado.collectAsState().value

    if (reporte == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Imagen de creación (siempre se muestra si está disponible)
        if (reporte.imagenCreacionUrl.isNotEmpty()) {
            AsyncImage(
                model = reporte.imagenCreacionUrl,
                contentDescription = "Imagen de creación",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
            )
        } else {
            Text("No se encontró imagen de creación.")
        }

        // Campos mínimos (siempre visibles)
        Text("Tipo de animal: ${reporte.tipoAnimal}")
        Text("Apariencia: ${reporte.aparienciaAnimal}")
        Text("Carácter: ${reporte.caracterAnimal}")
        //Text("Ubicación: ${reporte.ubicacion?.direccion ?: "Sin ubicación"}")
        Text("Estado: ${reporte.estado}")
        Text("Fecha de reporte: ${formatearFechaHumana2(reporte.fechaCreacion.toDate())}")

        // Si el estado NO es "No atendido", mostrar todos los campos extra
        if (reporte.estado == "Atendido") {
            Spacer(modifier = Modifier.height(16.dp))

            Text("ID Organización: ${reporte.idOrganizacion ?: "N/A"}")
            Text("Fecha de atención: ${reporte.fechaAtencion?.toDate() ?: "N/A"}")
            Text("Observaciones: ${reporte.observaciones ?: "N/A"}")
            //Text("Ubicación de atención: ${reporte.ubicacionAtencion?. ?: "Sin ubicación"}")
            Text("Frecuencia de avistamiento: ${reporte.frecuenciaAvistamiento}")
            Text("Info adicional: ${reporte.infoAdicional}")

            // Imagen de atención (si está disponible)
            if (!reporte.imagenAtencionUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = reporte.imagenAtencionUrl,
                    contentDescription = "Imagen de atención",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else {
                Text("No se encontró imagen de atención.")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón "Atender"
        if (isOrganizacion && reporte.estado == "No atendido") {
            Button(
                onClick = {
                    navController.navigate("atender_reporte/${reporte.id}")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Atender")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón "Regresar"
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar")
        }

        val user = (currentUserState as? CurrentUserState.Success)?.user

        // Botón de "Crear reporte" solo para ciudadanos
        if (user != null) {
            if(user.tipoUsuario == "Vecino"){
                if(origen == "historial_reportes"){
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Borrar")
                    }
                }
            }else if(user.tipoUsuario == "Aliado"){
                if(origen == "reportes"){
                    Button(
                        onClick = { navController.navigate("atender_reporte/${reporte.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Atender")
                    }
                }else if(origen == "historial_atenciones"){
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Borrar")
                    }
                }
            }
        }
    }
}