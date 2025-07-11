package com.example.geopatitas.ui.viewmodel

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

    fun detenerActualizaciones() {
        LocationUtils.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        detenerActualizaciones()
    }
}