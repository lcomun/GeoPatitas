package com.example.geopatitas.ui.screen.auth


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.geopatitas.data.UsuarioFirestoreDataSource
import com.example.geopatitas.navegation.AppDestinations
import com.example.geopatitas.data.repository.AuthRepository
import com.example.geopatitas.data.repository.AuthRepository2
import com.example.geopatitas.data.repository.UserRepository
import com.example.geopatitas.ui.viewmodel.LoginUiState
import com.example.geopatitas.ui.viewmodel.LoginVM
import com.example.geopatitas.ui.viewmodel.LoginViewModel
import com.example.geopatitas.ui.viewmodel.UserVM
import com.example.geopatitas.utils.GoogleSignInUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun LoginScreen2(
    navController: NavController,
    loginVM: LoginVM,
    userVM: UserVM,
    onLoginSuccess: () -> Unit // callback si el login fue exitoso
) {
    val loginState by loginVM.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        loginVM.procesarResultadoGoogle(result.data)
    }

    LaunchedEffect(key1 = loginState) {
        when (loginState) {
            is LoginUiState.AuthenticatedAndRegistered -> {
                val usuario = (loginState as LoginUiState.AuthenticatedAndRegistered).user
                userVM.loadUserData(usuario.correo)
                onLoginSuccess() // Puedes navegar a Home aquí
            }
            is LoginUiState.AuthenticatedAndNeedsRegistration -> {
                val email = (loginState as LoginUiState.AuthenticatedAndNeedsRegistration).email
                val nombre = (loginState as LoginUiState.AuthenticatedAndNeedsRegistration).name
                // Aquí podrías redirigir a una pantalla de registro adicional si es necesario
                Toast.makeText(context, "Usuario no registrado. Nombre: $nombre", Toast.LENGTH_LONG).show()
            }
            is LoginUiState.Error -> {
                val error = (loginState as LoginUiState.Error).message
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar sesión con Google")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                loginVM.iniciarGoogleSignIn(launcher)
            },
            enabled = loginState !is LoginUiState.Loading
        ) {
            Text("Continuar con Google")
        }

        if (loginState is LoginUiState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
