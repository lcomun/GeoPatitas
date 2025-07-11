package com.example.geopatitas.data.repository

import com.example.geopatitas.utils.GoogleSignInUtils
import com.example.geopatitas.utils.AuthRes
import com.example.geopatitas.utils.FirebaseAuthUtils
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class AuthRepository() {
    private val firebaseAuthHelper: FirebaseAuthUtils by lazy { FirebaseAuthUtils() }

    fun signOutFirebaseUser(): AuthRes<Unit> {
        return try {
            firebaseAuthHelper.signOut()
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Error desconocido al cerrar sesión.")
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuthHelper.getCurrentUser()
    }

    suspend fun signInWithGoogleCredentialFirebaseUser(credential: AuthCredential): AuthRes<FirebaseUser> {
        return try {
            firebaseAuthHelper.signInWithGoogleCredential(credential)
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Fallo al iniciar sesión con credenciales de Google.")
        }
    }
}