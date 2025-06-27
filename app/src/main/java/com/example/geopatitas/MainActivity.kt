package com.example.geopatitas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geopatitas.ui.comun.LoginScreen
import com.example.geopatitas.ui.comun.SignUpScreen
import com.example.geopatitas.ui.vecino.VecinoDashboardScreen
import com.example.geopatitas.ui.theme.GeoPatitasTheme
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.example.geopatitas.datasource.UsuarioFirestoreDataSource
import com.example.geopatitas.navegation.AppDestinations
import com.example.geopatitas.repository.AuthRepository
import com.example.geopatitas.repository.UserRepository
import com.example.geopatitas.ui.aliado.AliadoDashboardScreen
import com.example.geopatitas.viewmodel.CurrentUserState
import com.example.geopatitas.viewmodel.UserViewModel

// ---
// Bloque de la Actividad Principal
// ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialización de Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            // Se prepara la configuración inicial de la UI y la navegación
            AppContent()
        }
    }
}

// ---
// Bloque de Dependencias y ViewModel Factory
// ---
/**
 * Provee las dependencias necesarias para los repositorios y ViewModels.
 * En una aplicación real, se usaría una librería de inyección de dependencias como Hilt o Koin.
 */
@Composable
fun provideAppDependencies(): Triple<AuthRepository, UserRepository, androidx.lifecycle.ViewModelProvider.Factory> {
    val authRepository = remember { AuthRepository() }
    val userFirestoreDataSource = remember { UsuarioFirestoreDataSource() }
    val userRepository = remember { UserRepository(userFirestoreDataSource) }

    val userViewModelFactory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return UserViewModel(
                        authRepository = authRepository,
                        userRepository = userRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    return Triple(authRepository, userRepository, userViewModelFactory)
}

// ---
// Bloque del Contenido Principal de la Aplicación (Composable Root)
// ---
@Composable
fun AppContent() {
    val (authRepository, userRepository, userViewModelFactory) = provideAppDependencies()
    val navController = rememberNavController()

    // ViewModel scopeado a la Activity para determinar la ruta de inicio
    val activityScopedUserViewModel: UserViewModel = viewModel(factory = userViewModelFactory)
    val currentUserState by activityScopedUserViewModel.currentUserData.collectAsState()

    // Determinar la ruta de inicio basada en el estado del usuario
    val startDestination = determineStartDestination(currentUserState)

    GeoPatitasTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            // Solo inicializa NavHost cuando startDestination se ha determinado y no es la ruta de carga
            if (startDestination != AppDestinations.LOADING_ROUTE) {
                AppNavHost(navController, startDestination, userViewModelFactory)
            } else {
                // Muestra el indicador de carga mientras se determina la ruta de inicio
                LoadingScreen()
            }
        }
    }
}

// ---
// Bloque de Lógica de Inicio de Sesión
// ---
/**
 * Determina la ruta de inicio de la navegación basándose en el estado actual del usuario.
 */
@Composable
fun determineStartDestination(currentUserState: CurrentUserState): String {
    return when (currentUserState) {
        is CurrentUserState.Loading -> AppDestinations.LOADING_ROUTE
        is CurrentUserState.Success -> {
            when ((currentUserState as CurrentUserState.Success).user.tipoUsuario) {
                "Vecino" -> AppDestinations.VecinoFlow.ROOT
                "Aliado" -> AppDestinations.AliadoFlow.ROOT
                else -> AppDestinations.LOGIN_ROUTE // Tipo de usuario no válido
            }
        }
        is CurrentUserState.Unauthenticated,
        is CurrentUserState.Error -> AppDestinations.LOGIN_ROUTE
    }
}

// ---
// Bloque de la Pantalla de Carga
// ---
@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ---
// Bloque del Grafo de Navegación (NavHost)
// ---
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    userViewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.LOADING_ROUTE) {
            LoadingScreen()
        }

        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(navController = navController)
        }

        composable(
            AppDestinations.CREAR_CUENTA_ROUTE,
            arguments = listOf(
                navArgument("nombre") { defaultValue = "" },
                navArgument("correo") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val correo = backStackEntry.arguments?.getString("correo") ?: ""
            SignUpScreen(navController, nombre, correo)
        }

        ciudadanoNavGraph(navController, userViewModelFactory)

        aliadoNavGraph(navController, userViewModelFactory)

    }
}


fun NavGraphBuilder.ciudadanoNavGraph(
    navController: NavHostController,
    userViewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
) {
    navigation(
        startDestination = AppDestinations.VecinoFlow.DASHBOARD,
        route = AppDestinations.VecinoFlow.ROOT
    ) {
        composable(AppDestinations.VecinoFlow.DASHBOARD) { backStackEntry ->
            val navGraphEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppDestinations.VecinoFlow.ROOT)
            }
            val scopedUserViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = navGraphEntry,
                factory = userViewModelFactory
            )
            VecinoDashboardScreen(
                navController = navController,
                internalNavController = rememberNavController(), // Para navegación interna del dashboard
                userViewModel = scopedUserViewModel
            )
        }

    }



}

fun NavGraphBuilder.aliadoNavGraph(
    navController: NavHostController,
    userViewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
) {
    navigation(
        startDestination = AppDestinations.AliadoFlow.DASHBOARD,  // Asegúrate de que esta ruta apunte correctamente
        route = AppDestinations.AliadoFlow.ROOT
    ) {
        composable(AppDestinations.AliadoFlow.DASHBOARD) { backStackEntry ->
            val navGraphEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppDestinations.AliadoFlow.ROOT)
            }
            val scopedUserViewModel: UserViewModel = viewModel(
                viewModelStoreOwner = navGraphEntry,
                factory = userViewModelFactory
            )

            AliadoDashboardScreen(  // Asegúrate de que esta pantalla sea la de "Mapa ONG"
                navController = navController,
                internalNavController = rememberNavController(), // Para navegación interna
                userViewModel = scopedUserViewModel
            )
        }
    }
}