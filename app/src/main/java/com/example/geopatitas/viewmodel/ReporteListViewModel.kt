package com.example.geopatitas.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.model.Reporte
import com.example.geopatitas.utils.obtenerReportePorId
import com.example.geopatitas.utils.obtenerTodosLosReportes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReporteListViewModel2 : ViewModel() {

    private val _listaReportes = MutableStateFlow<List<Reporte>>(emptyList())
    val listaReportes: StateFlow<List<Reporte>> = _listaReportes.asStateFlow()

    private val _reporteSeleccionado = MutableStateFlow<Reporte?>(null)
    val reporteSeleccionado: StateFlow<Reporte?> = _reporteSeleccionado.asStateFlow()

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

}