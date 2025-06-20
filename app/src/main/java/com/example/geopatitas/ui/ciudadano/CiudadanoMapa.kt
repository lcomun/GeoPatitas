package com.example.geopatitas.ui.ciudadano

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.geopatitas.R
import com.example.geopatitas.ui.theme.AppBackground
import com.example.geopatitas.utils.LocationUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

data class Mascota(
    val id: String,
    val nombre: String,
    val tipo: String, // Perro, Gato, etc.
    val latitud: Double,
    val longitud: Double,
    val descripcion: String
)

fun generarMascotasCercanas(lat: Double, lon: Double): List<Mascota> {
    return listOf(
        Mascota("1", "Toby", "Perro", lat + 0.004, lon - 0.0015, "Perro mestizo amigable"),
        Mascota("2", "Luna", "Perro", lat + 0.005, lon + 0.0005, "Hembra blanca y negra"),
        Mascota("3", "Peluchin", "Gato", lat - 0.003, lon + 0.002, "Gato callejero muy tímido"),
        Mascota("4", "Michi", "Gato", lat + 0.0008, lon - 0.003, "Gato negro con mirada curiosa"),
        Mascota("5", "Bunny", "Conejo", lat + 0.0019, lon + 0.0015, "Conejo blanco con orejas largas"),
        Mascota("6", "Saltarín", "Conejo", lat - 0.001, lon - 0.0005, "Conejo travieso cerca de un parque"),
        Mascota("7", "Piolín", "Ave", lat + 0.0025, lon - 0.002, "Canario amarillo cantando cerca de árboles"),
        Mascota("8", "Colibrí", "Ave", lat - 0.0015, lon + 0.003, "Ave pequeña revoloteando flores"),
        Mascota("9", "Rocky", "Perro", lat - 0.006, lon - 0.001, "Perro tipo pitbull, pero muy tranquilo"),
        Mascota("9", "Mailo", "Serpiente", lat - 0.008, lon - 0.003, "Serpiente no venenosa"),
        Mascota("10", "Nieve", "Gato", lat + 0.008, lon + 0.0015, "Gato blanco escondido bajo un techo")


    )
}

@Composable
fun CiudadanoMapaScreen(navController: NavController) {

    val context = LocalContext.current

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }
    var Mascotas by remember { mutableStateOf(listOf<Mascota>()) }

    LaunchedEffect(Unit) {
        LocationUtils.initialize(context)

        LocationUtils.getLastLocation(context) { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
                Mascotas = generarMascotasCercanas(latitude, longitude)
                hasLocation = true
            }
        }

        LocationUtils.startLocationUpdates(context) { newLocation ->
            latitude = newLocation.latitude
            longitude = newLocation.longitude
            Mascotas = generarMascotasCercanas(latitude, longitude)
        }
    }

    AppBackground(
        imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg",
        overlayAlpha = 0.85f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mapa",
                style = MaterialTheme.typography.headlineMedium
            )


            if (hasLocation) {
                GoogleMapView(latitude = latitude, longitude = longitude, Mascotas = Mascotas)
            } else {
                CircularProgressIndicator()
            }

            Button(
                onClick = { navController.navigate("dashboard_ciudadano") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Dashboard")
            }
        }
    }
}

@Composable
fun GoogleMapView(latitude: Double, longitude: Double, Mascotas: List<Mascota>) {
    AndroidView(factory = { context ->
        val mapView = MapView(context).apply {
            id = View.generateViewId()
        }

        mapView.onCreate(Bundle())

        mapView.getMapAsync { googleMap ->
            val location = LatLng(latitude, longitude)

            // Añadir marcador en tu ubicación
            googleMap.addMarker(MarkerOptions().position(location).title("Tu ubicación"))

            // Centrar el mapa con zoom
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(location, 15.5f) // Zoom entre 2 (muy lejos) y 21 (super cerca)
            )


            Mascotas.forEach { perrito ->
                val perritoLocation = LatLng(perrito.latitud, perrito.longitud)

                val markerIcon = when (perrito.tipo) {
                    "Perro" -> BitmapDescriptorFactory.fromResource(R.drawable.perro)
                    "Gato" -> BitmapDescriptorFactory.fromResource(R.drawable.gato)
                    "Conejo" -> BitmapDescriptorFactory.fromResource(R.drawable.conejo)
                    "Ave" -> BitmapDescriptorFactory.fromResource(R.drawable.ave)
                    else -> BitmapDescriptorFactory.fromResource(R.drawable.huella24x24)
                }

                googleMap.addMarker(
                    MarkerOptions()
                        .position(perritoLocation)
                        .title(perrito.nombre)
                        .snippet(perrito.descripcion)
                        .icon(markerIcon)


                )
            }
        }

        mapView
    }, modifier = Modifier
        .fillMaxWidth()
       //.weight(1f)
     )
}
