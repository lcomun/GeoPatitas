package com.example.geopatitas.ui.screen.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.geopatitas.data.UsuarioFirestoreDataSource
import com.example.geopatitas.navegation.AppDestinations
import com.example.geopatitas.data.repository.AuthRepository
import com.example.geopatitas.data.repository.UserRepository
import com.example.geopatitas.ui.viewmodel.LoginUiState
import com.example.geopatitas.ui.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun LoginScreen(navController: NavController) {
    // 1. Obtener el Contexto local y el ApplicationContext
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    // 2. Inyección manual del ViewModel (en un proyecto real, usarías Hilt/Koin)
    // Se crean las instancias de los repositorios y datasources que necesita el ViewModel.
    val authRepository = AuthRepository()
    val userFirestoreDataSource = UsuarioFirestoreDataSource()
    val userRepository = UserRepository(userFirestoreDataSource)

    val viewModel: LoginViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return LoginViewModel(
                        authRepository = authRepository,
                        userRepository = userRepository,
                        applicationContext = applicationContext
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    // 3. Observar el estado de la UI expuesto por el ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Abre la ventana de seleccion de cuentas de google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        // El ViewModel es quien procesa el resultado de Google, no la pantalla directamente.
        viewModel.handleGoogleSignInResult(task)
    }

    // 5. Efectos Secundarios: Reaccionar a los cambios de estado del ViewModel
    // LaunchedEffect se ejecuta cada vez que 'uiState' cambia.
    //LOGIN PASO 5
    LaunchedEffect(uiState) {
        when (uiState) {
            LoginUiState.Loading -> {
                // Puedes mostrar un Toast o simplemente el CircularProgressIndicator en la UI
                // Toast.makeText(context, "Cargando...", Toast.LENGTH_SHORT).show()
            }

            LoginUiState.StartGoogleSignInFlow -> {
                // El ViewModel indica que es momento de lanzar el Intent de Google Sign-In.
                googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
            }

            is LoginUiState.GoogleAccountReceived -> {
                // Una vez que Google devuelve la cuenta, el ViewModel procede con la autenticación de Firebase.
                val account = (uiState as LoginUiState.GoogleAccountReceived).account
                viewModel.authenticateWithFirebaseAndCheckRegistration(account)
            }

            is LoginUiState.AuthenticatedAndRegistered -> {
                // El usuario está autenticado y registrado en tu BD. Navegar según su tipo.
                val user = (uiState as LoginUiState.AuthenticatedAndRegistered).user
                when (user.tipoUsuario) {
                    "Vecino" -> navController.navigate(AppDestinations.VecinoFlow.DASHBOARD) {
                        popUpTo("login_route") { inclusive = true } // Limpia el back stack
                    }

                    "Aliado" -> navController.navigate(AppDestinations.AliadoFlow.DASHBOARD) {
                        popUpTo("login_route") { inclusive = true }
                    }

                    else -> Toast.makeText(
                        context,
                        "Tipo de usuario no válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is LoginUiState.AuthenticatedAndNeedsRegistration -> {
                // El usuario está autenticado con Firebase pero no registrado en tu BD. Navegar a Crear Cuenta.
                val state = uiState as LoginUiState.AuthenticatedAndNeedsRegistration
                navController.navigate("crear_cuenta?nombre=${state.name}&correo=${state.email}") {
                    popUpTo("login_route") { inclusive = true }
                }
            }

            is LoginUiState.Error -> {
                // Mostrar mensajes de error al usuario.
                val errorMessage = (uiState as LoginUiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }

            LoginUiState.Idle -> {
                // Estado inicial o cuando no hay operación en curso. No hace nada visible.
            }
        }
    }

    //LOGIN PASO 1
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onGoogleSignInClicked() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading
        ) {
            Text("Iniciar sesión con Google")
        }
    }
}