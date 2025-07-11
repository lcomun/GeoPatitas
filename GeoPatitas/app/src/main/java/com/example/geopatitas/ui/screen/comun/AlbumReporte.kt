package com.example.geopatitas.ui.screen.comun

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.geopatitas.ui.viewmodel.LocationViewModel
import com.example.geopatitas.ui.viewmodel.ReporteListViewModel2
import com.example.geopatitas.utils.LocationUtils

@Composable
fun AlbumReporteScreen(
    locationVM: LocationViewModel? = null,
    reporteListViewModel: ReporteListViewModel2,
    onReporteClick: (String) -> Unit,
    soloDelUsuario: Boolean
) {

    val userLocation by locationVM?.ubicacion?.collectAsState() ?: remember { mutableStateOf(null) }
    val listaReportes by reporteListViewModel.listaReportes.collectAsState()
    val listaFiltrada by reporteListViewModel.listaFiltrada.collectAsState()

    val reportesParaMostrar = if (soloDelUsuario) listaFiltrada else listaReportes

    LaunchedEffect(Unit) {
        if(reporteListViewModel.esListaVacia())
            reporteListViewModel.cargarReportes()
    }

    // ✅ Lógica reactiva al cambio de ubicación
    LaunchedEffect(userLocation) {
        userLocation?.let {
            reporteListViewModel.ordenarReportes(it)
        }
    }


    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Reportes Registrados",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(reportesParaMostrar) { reporte ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onReporteClick(reporte.id)
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (reporte.imagenCreacionUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = reporte.imagenCreacionUrl,
                                    contentDescription = "Imagen del reporte",
                                    modifier = Modifier
                                        .height(250.dp)
                                        .fillMaxWidth()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .height(140.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Sin imagen", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reporte.tipoAnimal,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text =  reporteListViewModel.formatearFecha(reporte.fechaCreacion.toDate()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = reporte.estado,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}