package com.example.geopatitas.data

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UsuarioFirestoreDataSource {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("usuarios")

    suspend fun saveUser(usuario: Usuario) {
        try {
            usersCollection.add(usuario).await()
        } catch (e: Exception) {
            throw Exception("No se pudo guardar el usuario en la base de datos.", e)
        }
    }

    suspend fun getUserByEmail(correo: String): Usuario? {
        return try {
            val result = usersCollection.whereEqualTo("correo", correo).get().await()

            if (!result.isEmpty) {
                result.documents.first().toObject<Usuario>()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error al obtener usuario desde Firestore por correo: ${e.message}")
            null
        }
    }
}