package com.example.geopatitas.model

data class Usuario(
    //Ciudadano y Organizacion
    val idUsuario : String = "",
    val nombre: String = "", //Por defecto es el valor del auth, se puede editar
    val correo: String = "", //Se debe guardar del auth
    val tipoUsuario: String = "", //Ciudadano u Organizacion
    val infoContacto: List<Contacto>? = null,
    //las ong deben tener al menos un contacto obligatorio al registrarse
    //el contacto por defecto es el correo con el que se accede

    //Organizacion
    //Esto se muestra solo si el usuario selecciona tipoUsuario = Organizacion
    val logo: String? = null, //saltar
    val infoDonacion: List<Donacion>? = null, // saltar No es obligatorio al crear cuenta
    val numCasosAtendidos: Int = 0,
)

data class Donacion(
    val nombre: String = "",
    val tipo: String = "",
    val valor: String = "",
    val descripcion: String? = null
)

data class Contacto(
    val nombre: String = "",
    val tipo: String = "",
    val valor: String = "",
    val descripcion: String? = null
)