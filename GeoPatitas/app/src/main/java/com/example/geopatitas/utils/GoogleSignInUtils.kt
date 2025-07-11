package com.example.geopatitas.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.geopatitas.R
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

sealed class AuthRes<out T> {
    data class Success<T>(val data: T): AuthRes<T>()
    data class Error(val errorMessage: String): AuthRes<Nothing>()
}

class GoogleSignInUtils(private val contexto: Context) {

    private val clienteIdentidadGoogle = Identity.getSignInClient(contexto)

    private val clienteGoogleSignIn: GoogleSignInClient by lazy {
        val opcionesGoogle = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(contexto.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(contexto, opcionesGoogle)
    }

    fun cerrarSesion() {
        clienteIdentidadGoogle.signOut()
    }

    fun procesarResultadoInicioSesionGoogle(task: Task<GoogleSignInAccount>): AuthRes<GoogleSignInAccount> {
        return try {
            val cuenta = task.getResult(ApiException::class.java)
            AuthRes.Success(cuenta)
        } catch (e: ApiException) {
            AuthRes.Error(e.message ?: "Error al iniciar sesión con Google.")
        }
    }

    /** Lanza el flujo de inicio de sesión con Google usando el launcher proporcionado */
    fun iniciarFlujoInicioSesionGoogle(launcher: ActivityResultLauncher<Intent>) {
        val intentGoogleSignIn = clienteGoogleSignIn.signInIntent
        launcher.launch(intentGoogleSignIn)
    }
}
