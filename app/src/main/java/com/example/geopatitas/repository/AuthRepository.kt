package com.example.geopatitas.repository

import com.example.geopatitas.utils.AuthRes
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    suspend fun signOut(): AuthRes<Unit> {
        return try {
            auth.signOut()
            AuthRes.Success(Unit) // Unit indica que no hay datos de retorno específicos en caso de éxito
        } catch (e: Exception) {
            // Captura cualquier excepción durante el signOut y devuelve un error
            AuthRes.Error(e.message ?: "Error desconocido al cerrar sesión.")
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signInWithGoogleCredential(credential: AuthCredential): AuthRes<FirebaseUser> {
        return try {
            // signInWithCredential().await() espera de forma segura a que la tarea de Firebase se complete.
            val firebaseUserResult = auth.signInWithCredential(credential).await()

            // Verifica si el usuario resultante de la autenticación de Firebase es nulo
            firebaseUserResult.user?.let { user ->
                AuthRes.Success(user) // Retorna el usuario de Firebase si la autenticación fue exitosa
            } ?: AuthRes.Error("Fallo al iniciar sesión con credenciales: Usuario Firebase nulo.")
        } catch (e: Exception) {
            // Captura cualquier excepción durante el proceso de autenticación y devuelve un error
            AuthRes.Error(e.message ?: "Fallo al iniciar sesión con credenciales de Google.")
        }
    }
}