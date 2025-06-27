package com.example.geopatitas.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

data class Ubicacion(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
){
    constructor(latLng: LatLng) : this(latLng.latitude, latLng.longitude)
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}

data class Reporte(
    //Campos mínimos obligatorios
    val id: String = "",
    val idUsuario: String = "",
    val tipoAnimal: String = "",
    val caracterAnimal: String = "",
    val aparienciaAnimal: String = "",
    val imagenCreacionUrl: String = "",
    val ubicacion: Ubicacion? = null,
    val estado: String = "No atendido",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val frecuenciaAvistamiento: String = "",
    val infoAdicional: String = "",

    //Campos opcionales, Accion atender
    val idOrganizacion: String? = null,
    val fechaAtencion: Timestamp? = null,
    val observaciones: String? = null,
    val imagenAtencionUrl: String? = null,
    val ubicacionAtencion: Ubicacion? = null
)
