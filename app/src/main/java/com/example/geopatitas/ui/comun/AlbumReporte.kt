package com.example.geopatitas.ui.comun

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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import android.location.Location
import com.example.geopatitas.model.Reporte
import com.google.android.gms.maps.model.LatLng

@Composable
fun AlbumReporteScreen(
    navController: NavController,
    userLocation: Location?,
    listaReportes: List<Reporte>) {

    val _userLocation: LatLng? = userLocation?.let {
        LatLng(it.latitude, it.longitude)
    }

    // Ordenar reportes por distancia si hay ubicación válida
    val reportesOrdenados = remember(listaReportes, _userLocation) {
        if (userLocation != null) {
            listaReportes.sortedBy { reporte ->
                val lat = reporte.ubicacion?.latitude?: 0.0
                val lon = reporte.ubicacion?.longitude?: 0.0
                calcularDistancia2(
                    userLocation.latitude,
                    userLocation.longitude,
                    lat,
                    lon
                )
            }
        } else {
            listaReportes
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
                items(reportesOrdenados) { reporte ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("detalle_reporte/${reporte.id}")
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
                                text = formatearFechaHumana2(reporte.fechaCreacion.toDate()),
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

fun formatearFechaHumana2(fecha: Date): String {
    val ahora = Calendar.getInstance()
    val calFecha = Calendar.getInstance().apply { time = fecha }

    val esHoy = ahora.get(Calendar.YEAR) == calFecha.get(Calendar.YEAR) &&
            ahora.get(Calendar.DAY_OF_YEAR) == calFecha.get(Calendar.DAY_OF_YEAR)

    ahora.add(Calendar.DAY_OF_YEAR, -1)
    val esAyer = ahora.get(Calendar.YEAR) == calFecha.get(Calendar.YEAR) &&
            ahora.get(Calendar.DAY_OF_YEAR) == calFecha.get(Calendar.DAY_OF_YEAR)

    return when {
        esHoy -> "Hoy"
        esAyer -> "Ayer"
        else -> {
            val formato = SimpleDateFormat("EEE d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            formato.format(fecha).replaceFirstChar { it.uppercaseChar() }
        }
    }
}

fun calcularDistancia2(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Radio de la Tierra en km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c // resultado en km
}
