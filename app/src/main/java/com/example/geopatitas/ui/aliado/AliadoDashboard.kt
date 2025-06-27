package com.example.geopatitas.ui.aliado

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
import com.example.geopatitas.ui.vecino.VecinoPerfilScreen
import com.example.geopatitas.ui.comun.BuscarScreen
import com.example.geopatitas.ui.comun.ReportesScreen
import com.example.geopatitas.ui.comun.ReporteDetailScreen
import com.example.geopatitas.viewmodel.LocationViewModel
import com.example.geopatitas.viewmodel.ReporteListViewModel2
import com.example.geopatitas.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliadoDashboardScreen(navController: NavController, internalNavController: NavController, userViewModel: UserViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val localNavController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("mapa_ong") }

    val reporteListViewModel: ReporteListViewModel2 = viewModel()
    val locationVM: LocationViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Mapa") },
                    selected = selectedRoute == "reportes",
                    onClick = {
                        selectedRoute = "mapa_ong"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("reportes") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") }
                )

                NavigationDrawerItem(
                    label = { Text("Buscar") },
                    selected = selectedRoute == "buscar",
                    onClick = {
                        selectedRoute = "buscar"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("buscar") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
                )

                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = selectedRoute == "perfil_ong",
                    onClick = {
                        selectedRoute = "perfil_ong"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("perfil_ong") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }
                )

                NavigationDrawerItem(
                    label = { Text("Historial") },
                    selected = selectedRoute == "historial_ong",
                    onClick = {
                        selectedRoute = "historial_ong"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("historial_ong") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Historial") }
                )

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0) // limpia toda la pila
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
                    title = { Text("Dashboard Aliado") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = localNavController,
                    startDestination = "reportes"
                ) {
                    composable("reportes") {
                        ReportesScreen(localNavController, userViewModel, reporteListViewModel, locationVM) // Asegúrate de que esta pantalla se cargue primero
                    }
                    composable("aliado_perfil") {
                        VecinoPerfilScreen(userViewModel)
                    }
                    composable("aliado_historial") {
                        ReportesAtendidosScreen(localNavController)
                    }
                    composable("buscar") {
                        BuscarScreen(localNavController)
                    }
                    /*
                    composable("detalle_reporte/{reporteId}") { backStackEntry ->
                        val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                        ReporteDetailScreen(navController = localNavController, reporteId = reporteId, userViewModel)
                    }
                     */
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

                    // Agrega la pantalla detalle reporte aquí:
                    composable("atender_reporte/{reporteId}") { backStackEntry ->
                        val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                        ReporteAtendidoFormScreen(
                            navController = localNavController,
                            reporteId = reporteId,
                            userViewModel,
                            locationVM
                        )
                    }
                }
            }
        }
    }
}