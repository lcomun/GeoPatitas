package com.example.geopatitas.ui.viewmodel


import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.data.Reporte
import com.example.geopatitas.utils.obtenerReportePorId
import com.example.geopatitas.utils.obtenerTodosLosReportes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ReporteListViewModel2 : ViewModel() {

    private val _listaReportes = MutableStateFlow<List<Reporte>>(emptyList())
    val listaReportes: StateFlow<List<Reporte>> = _listaReportes.asStateFlow()

    private val _reporteSeleccionado = MutableStateFlow<Reporte?>(null)
    val reporteSeleccionado: StateFlow<Reporte?> = _reporteSeleccionado.asStateFlow()

    private val _idUsuarioActual = MutableStateFlow<String?>(null)
    var tipoUsuario = String()

    private val _listaFiltrada = MutableStateFlow<List<Reporte>>(emptyList())
    val listaFiltrada = _listaFiltrada.asStateFlow()

    init {
        // Observa cambios en _listaReportes y _idUsuarioActual
        viewModelScope.launch {
            _listaReportes.collect {
                if(!esListaVacia())
                    filtrarReportes()
            }
        }
        viewModelScope.launch {
            _idUsuarioActual.collect {
                if(!esListaVacia())
                    filtrarReportes()
            }
        }
    }

    // Función para filtrar la lista segun idUsuarioActual y listaReportes
    private fun filtrarReportes() {
        val idUsuario = _idUsuarioActual.value
        val lista = _listaReportes.value
        //Log.d("hola", "el id del usuario es " + idUsuario)
        //Log.d("hola", "Lista es vacia? " + esListaVacia())
        if (idUsuario != null) {
            if(tipoUsuario == "Vecino")
                _listaFiltrada.value = lista.filter { it.idUsuario == idUsuario }
            else if(tipoUsuario == "Aliado")
                _listaFiltrada.value = lista.filter { it.idOrganizacion == idUsuario }
            //Log.d("VM", "✅ Lista filtrada actualizada: ${_listaFiltrada.value.size}")
        } else {
            _listaFiltrada.value = emptyList()
        }
    }

    fun esListaVacia() : Boolean{
        return _listaReportes.value.isEmpty()
    }

    fun cargarUsuario(idUser: String, tipoUsuario: String) {
        this.tipoUsuario = tipoUsuario
        _idUsuarioActual.value = idUser
    }

    fun cargarReportes() {
        viewModelScope.launch {
            val reportes = obtenerTodosLosReportes()
            _listaReportes.value = reportes
        }
    }

    fun cargarReportePorId(id: String) {
        viewModelScope.launch {
            val reporte = obtenerReportePorId(id)
            _reporteSeleccionado.value = reporte
        }
    }

    suspend fun obtenerReportePorIdVm(id: String): Reporte? {
        return obtenerReportePorId(id)
    }

    fun formatearFecha(fecha: Date): String {
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

    fun ordenarReportes(userLocation: Location){
        val listaOrdenada = _listaReportes.value.sortedBy { reporte ->
            val lat = reporte.ubicacion?.latitude ?: 0.0
            val lon = reporte.ubicacion?.longitude ?: 0.0
            calcularDistancia(
                userLocation.latitude,
                userLocation.longitude,
                lat,
                lon
            )
        }
        _listaReportes.value = listaOrdenada
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // resultado en km
    }

}