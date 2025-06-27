package com.example.geopatitas.ui.comun

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.geopatitas.model.Reporte
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


@Composable
fun MapaReporteScreen(
    navController: NavController,
    location: Location?,
    listaReportes: List<Reporte>
) {
    val userLocation: LatLng? = location?.let {
        LatLng(it.latitude, it.longitude)
    }

    GoogleMapView2(userLocation, listaReportes, navController)
}

@Composable
fun GoogleMapView2(userLocation: LatLng?, reportes: List<Reporte>, navController: NavController) {
    // Este key fuerza la recreación si cambia userLocation o reportes
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                id = View.generateViewId()
                onCreate(Bundle())
                onStart()
                onResume()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        update = { mapView ->
            mapView.getMapAsync { googleMap ->
                googleMap.clear()

                val center = userLocation ?: LatLng(-12.1041, -77.0479)

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15.5f))

                // Marcador ubicación usuario (si existe)
                userLocation?.let {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(it)
                            .title("Tu ubicación")
                    )
                }

                val markerReporteMap = mutableMapOf<Marker, String>()

// Crear los marcadores y guardar el mapeo con el reporte
                reportes.forEach { reporte ->
                    val lat = reporte.ubicacion?.latitude ?: 0.0
                    val lng = reporte.ubicacion?.longitude ?: 0.0
                    val loc = LatLng(lat, lng)

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(loc)
                            .title(reporte.tipoAnimal)
                            .snippet(reporte.fechaCreacion.toDate().toString())
                    )

                    // Guardar el marcador con su reporte (si el marker no es null)
                    if (marker != null) {
                        markerReporteMap[marker] = reporte.id
                        //Log.d("MarkerMap", "Guardado: marker=$marker, id=${reporte.id}")
                    }
                }

                //Escuchar clics en info windows
                googleMap.setOnInfoWindowClickListener { marker ->
                    val reporteId = markerReporteMap[marker]
                    //Log.d("MarkerMap", "SELECCIONARRR: marker=$marker, id=${reporteId}")
                    if (!reporteId.isNullOrBlank()) {
                        navController.navigate("detalle_reporte/${reporteId}")
                    }
                }

            }
        }
    )
}