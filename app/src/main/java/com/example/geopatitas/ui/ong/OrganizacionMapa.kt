package com.example.geopatitas.ui.ong

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.geopatitas.R
import com.example.geopatitas.ui.theme.AppBackground
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.geopatitas.utils.LocationUtils



data class Organizacion(
    val id: String,
    val nombre: String,
    val tipoServicio: String, // Ej: "Adopción", "Rescate", etc.
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
    val telefono: String? = null
)

fun generarONGsCercanas(lat: Double, lon: Double): List<Organizacion> {
    return listOf(
        Organizacion("1", "Refugio Esperanza", "Rescate y adopción", lat + 0.003, lon - 0.001, "Centro dedicado al rescate de animales callejeros"),
        Organizacion("2", "Amigos de la Fauna", "Protección animal", lat + 0.004, lon + 0.0015, "ONG dedicada a la conservación de fauna urbana"),
        Organizacion("3", "Patitas Seguras", "Cuidado veterinario", lat - 0.002, lon + 0.003, "Ofrece servicios veterinarios gratuitos para animales callejeros"),
        Organizacion("4", "Alas y Colas", "Animales exóticos y aves", lat + 0.001, lon - 0.003, "Especializados en aves y pequeños roedores rescatados")
    )
}

@Composable
fun OrganizacionMapaScreen(navController: NavController) {
    val context = LocalContext.current

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        LocationUtils.initialize(context)

        LocationUtils.getLastLocation(context) { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
                hasLocation = true
            }
        }

        LocationUtils.startLocationUpdates(context) { newLocation ->
            latitude = newLocation.latitude
            longitude = newLocation.longitude
        }
    }

    val ongs = generarONGsCercanas(latitude, longitude)

    AppBackground(imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg",  overlayAlpha = 0.85f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ONGs Cercanas",
                style = MaterialTheme.typography.headlineMedium
            )

            if (hasLocation) {
                GoogleMapViewONG(latitude = latitude, longitude = longitude, ongs = ongs)
            } else {
                CircularProgressIndicator()
            }

            Button(
                onClick = { navController.navigate("dashboard_ong") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Dashboard")
            }
        }
    }
}
@Composable
fun GoogleMapViewONG(latitude: Double, longitude: Double, ongs: List<Organizacion>) {
    AndroidView(factory = { context ->
        val mapView = MapView(context).apply {
            id = View.generateViewId()
        }

        mapView.onCreate(Bundle())

        mapView.getMapAsync { googleMap ->
            val currentLocation = LatLng(latitude, longitude)

            // Marcador de tu ubicación
            googleMap.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            // Centrar el mapa con zoom
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.5f))

            // Marcadores dinámicos para ONGs
            ongs.forEach { ong ->
                val ongLocation = LatLng(ong.latitud, ong.longitud)

                val markerIcon = when (ong.tipoServicio) {
                    "Rescate" -> BitmapDescriptorFactory.fromResource(R.drawable.ong)
                    "Adopción" -> BitmapDescriptorFactory.fromResource(R.drawable.ong)
                    "Protección animal" -> BitmapDescriptorFactory.fromResource(R.drawable.ong)
                    "Cuidado veterinario" -> BitmapDescriptorFactory.fromResource(R.drawable.ong)
                    else -> BitmapDescriptorFactory.fromResource(R.drawable.ong)
                }

                googleMap.addMarker(
                    MarkerOptions()
                        .position(ongLocation)
                        .title("${ong.nombre} - ${ong.tipoServicio}")
                        .snippet(ong.descripcion)
                        .icon(markerIcon)
                )
            }
        }

        mapView
    }, modifier = Modifier
        .fillMaxWidth()
        .height(400.dp))
}