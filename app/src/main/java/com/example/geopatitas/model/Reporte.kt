package com.example.geopatitas.model

import com.google.firebase.Timestamp

data class Reporte(
    val tipoAnimal: String,
    val caracterAnimal: String,
    val aparienciaAnimal: String,
    val imagenCreacionUrl: String? = null,
    val ubicacion: Map<String, Double>,
    val estado: String = "Pendiente",
    val fechaCreacion: Timestamp,

    //ReporteAtendido
    val idOrganizacion: String? = null,
    val fechaAtencion: Timestamp? = null,
    val observaciones: String? = null,
    val imagenAtencionUrl: String? = null,
    val ubicacionAtencion: String? = null
)
