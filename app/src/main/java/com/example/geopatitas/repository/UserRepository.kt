package com.example.geopatitas.repository

import com.example.geopatitas.datasource.UsuarioFirestoreDataSource
import com.example.geopatitas.model.Usuario

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