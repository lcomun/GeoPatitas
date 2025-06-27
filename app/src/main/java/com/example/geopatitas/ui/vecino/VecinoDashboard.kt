package com.example.geopatitas.ui.vecino


import com.example.geopatitas.ui.comun.ReportesScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geopatitas.ui.comun.BuscarScreen
import com.example.geopatitas.ui.comun.ReporteDetailScreen
import com.example.geopatitas.viewmodel.CurrentUserState
import com.example.geopatitas.viewmodel.LocationViewModel
import com.example.geopatitas.viewmodel.ReporteListViewModel2
import com.example.geopatitas.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VecinoDashboardScreen(navController: NavController, internalNavController: NavController, userViewModel: UserViewModel) {
    val currentUserState by userViewModel.currentUserData.collectAsState()
    val reporteListViewModel: ReporteListViewModel2 = viewModel()

    // ViewModels
    val locationVM: LocationViewModel = viewModel()
    // Observar ubicación
    //val ubicacion by ubicacionVM.ubicacion.collectAsState()

    LaunchedEffect(currentUserState) {
        when (currentUserState) {
            is CurrentUserState.Loading -> {
                Log.d("CiudadanoDashboard", "Estado del usuario: Cargando...")
            }
            is CurrentUserState.Success -> {
                val user = (currentUserState as CurrentUserState.Success).user
                Log.d("CiudadanoDashboard", "Usuario autenticado:")
                Log.d("CiudadanoDashboard", " ID: ${user.idUsuario}")
                Log.d("CiudadanoDashboard", " Nombre: ${user.nombre}")
                Log.d("CiudadanoDashboard", " Correo: ${user.correo}")
                Log.d("CiudadanoDashboard", " Tipo: ${user.tipoUsuario}")
            }
            is CurrentUserState.Unauthenticated -> {
                Log.d("CiudadanoDashboard", "Estado del usuario: No autenticado.")
            }
            is CurrentUserState.Error -> {
                val error = (currentUserState as CurrentUserState.Error).message
                Log.e("CiudadanoDashboard", "Error al cargar usuario: $error")
            }
        }
    }

    val userId = (currentUserState as? CurrentUserState.Success)?.user?.idUsuario ?: ""

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val localNavController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("mapa_ong") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Mapa") },
                    selected = selectedRoute == "reportes",
                    onClick = {
                        selectedRoute = "reportes"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("reportes") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") }
                )
                NavigationDrawerItem(
                    label = { Text("Buscar") },
                    selected = selectedRoute == "buscar",
                    onClick = {
                        selectedRoute = "buscar"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("buscar") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
                )
                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = selectedRoute == "vecino_perfil",
                    onClick = {
                        selectedRoute = "vecino_perfil"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("vecino_perfil") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }
                )
                NavigationDrawerItem(
                    label = { Text("Historial") },
                    selected = selectedRoute == "vecino_reportes",
                    onClick = {
                        selectedRoute = "vecino_reportes"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("vecino_reportes") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Historial") }
                )
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        userViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión") }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard Vecino") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(navController = localNavController, startDestination = "reportes") {
                    composable("reportes") {
                        ReportesScreen(navController = localNavController, userViewModel, reporteListViewModel, locationVM)
                    }
                    composable("vecino_perfil") {
                        VecinoPerfilScreen(userViewModel = userViewModel)
                    }
                    composable("vecino_historial") {
                        ReportesRealizadosScreen(localNavController)
                    }
                    composable("buscar") {
                        BuscarScreen(localNavController)
                    }
                    composable("reportar") {
                        ReporteFormScreen(localNavController, userId, locationVM)
                    }
                    composable(
                        "detalle_reporte/{reporteId}?origen={origen}",
                        arguments = listOf(
                            navArgument("reporteId") { type = NavType.StringType },
                            navArgument("origen") {
                                type = NavType.StringType
                                defaultValue = "reportes"
                            }
                        )
                    ) { backStackEntry ->
                        val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                        val origen = backStackEntry.arguments?.getString("origen") ?: "reportes"

                        ReporteDetailScreen(
                            navController = localNavController,
                            reporteId = reporteId,
                            origen = origen,
                            userViewModel = userViewModel
                        )
                    }

                    /*
                    // Agrega la pantalla detalle reporte aquí:
                    composable("detalle_reporte/{reporteId}") { backStackEntry ->
                        val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                        ReporteDetailScreen(navController = localNavController, reporteId = reporteId, userViewModel)
                    }
                     */
                }
            }
        }
    }
}
