package com.example.geopatitas.model

data class Usuario(
    //Ciudadano y Organizacion
    val nombre: String,
    val correo: String,
    val tipoUsuario: String,
    val infoContacto: List<Contacto>? = null,

    //Organizacion
    val logo: String? = null,
    val infoDonacion: List<Donacion>? = null,
    val numCasosAtendidos: Int = 0,
)

data class Donacion(
    val nombre: String,
    val tipo: String,
    val valor: String,
    val descripcion: String? = null
)

data class Contacto(
    val nombre: String = "",
    val tipo: String = "",
    val valor: String = "",
    val descripcion: String? = null
)