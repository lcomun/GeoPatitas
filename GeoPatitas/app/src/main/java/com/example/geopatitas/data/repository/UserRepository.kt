package com.example.geopatitas.data.repository

import com.example.geopatitas.data.UsuarioFirestoreDataSource
import com.example.geopatitas.data.Usuario

class UserRepository(
    private val userFirestoreDataSource: UsuarioFirestoreDataSource
) {

    suspend fun getRegisteredUser(email: String): Usuario? {
        return userFirestoreDataSource.getUserByEmail(email)
    }


    suspend fun saveNewUser(usuario: Usuario) {
        userFirestoreDataSource.saveUser(usuario)
    }
}