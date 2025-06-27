package com.example.geopatitas.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.geopatitas.utils.LocationUtils
import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel : ViewModel() {

    private val _ubicacion = MutableStateFlow<Location?>(null)
    val ubicacion: StateFlow<Location?> = _ubicacion

    fun setUbicacion(location: Location) {
        _ubicacion.value = location
    }

    /*
    fun iniciarActualizaciones(context: Context) {
        LocationUtils.initialize(context)

        // Obtener ubicación inmediata (última conocida)
        LocationUtils.getLastLocation(context) { location ->
            location?.let {
                _ubicacion.value = it
            }
        }

        // Iniciar actualizaciones en tiempo real
        LocationUtils.startLocationUpdates(context) { location ->
            _ubicacion.value = location
        }
    }
     */

    fun detenerActualizaciones() {
        LocationUtils.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        detenerActualizaciones()
    }
}