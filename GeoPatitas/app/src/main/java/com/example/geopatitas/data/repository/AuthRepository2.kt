package com.example.geopatitas.data.repository

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.geopatitas.utils.AuthRes
import com.example.geopatitas.utils.FirebaseAuthUtils
import com.example.geopatitas.utils.GoogleSignInUtils
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class AuthRepository2(
    private val googleSignInUtils: GoogleSignInUtils,
    private val firebaseAuthUtils: FirebaseAuthUtils = FirebaseAuthUtils()
) {

    fun cerrarSesion() {
        firebaseAuthUtils.signOut()
        googleSignInUtils.cerrarSesion()
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        return firebaseAuthUtils.getCurrentUser()
    }

    suspend fun iniciarSesionConCredencialesGoogle(credencial: AuthCredential): AuthRes<FirebaseUser> {
        return firebaseAuthUtils.signInWithGoogleCredential(credencial)
    }

    fun procesarResultadoGoogle(task: Task<GoogleSignInAccount>): AuthRes<GoogleSignInAccount> {
        return googleSignInUtils.procesarResultadoInicioSesionGoogle(task)
    }

    fun iniciarFlujoGoogle(launcher: ActivityResultLauncher<Intent>) {
        googleSignInUtils.iniciarFlujoInicioSesionGoogle(launcher)
    }
}