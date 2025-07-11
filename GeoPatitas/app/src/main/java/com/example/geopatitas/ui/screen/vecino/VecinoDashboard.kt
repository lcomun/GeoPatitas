package com.example.geopatitas.ui.screen.vecino


import com.example.geopatitas.ui.screen.comun.ReportesScreen
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
import com.example.geopatitas.ui.screen.comun.AlbumReporteScreen
import com.example.geopatitas.ui.screen.comun.BuscarScreen
import com.example.geopatitas.ui.screen.comun.DashboardScreen
import com.example.geopatitas.ui.screen.comun.ReporteDetailScreen
import com.example.geopatitas.ui.viewmodel.CurrentUserState
import com.example.geopatitas.ui.viewmodel.LocationViewModel
import com.example.geopatitas.ui.viewmodel.ReporteListViewModel2
import com.example.geopatitas.ui.viewmodel.UserViewModel
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

    val userId = (currentUserState as? CurrentUserState.Success)?.user?.idUsuario ?: ""

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val localNavController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("dashboard") }

    LaunchedEffect(currentUserState) {
        val user = (currentUserState as? CurrentUserState.Success)?.user
        if (user != null) {
            reporteListViewModel.cargarUsuario(user.idUsuario, user.tipoUsuario)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = selectedRoute == "dashboard",
                    onClick = {
                        selectedRoute = "dashboard"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("dashboard") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") }
                )
                NavigationDrawerItem(
                    label = { Text("Reportes") },
                    selected = selectedRoute == "reportes",
                    onClick = {
                        selectedRoute = "reportes"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("reportes") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Mapa") }
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
                    label = { Text("Mis reportes") },
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
                NavHost(
                    navController = localNavController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        DashboardScreen()
                    }
                    composable("vecino_perfil") {
                        VecinoPerfilScreen(userViewModel = userViewModel)
                    }

                    composable("buscar") {
                        BuscarScreen(navController = localNavController)
                    }

                    composable("reportar") {
                        ReporteFormScreen(
                            navController = localNavController,
                            userId = userId,
                            locationVM = locationVM
                        )
                    }

                    composable("reportes") {
                        ReportesScreen(
                            navController = localNavController,
                            userViewModel = userViewModel,
                            reporteListViewModel = reporteListViewModel,
                            locationVM = locationVM,
                            onReporteClick = { reporteId ->
                                localNavController.navigate("detalle_reporte/$reporteId?editable=a")
                            }
                        )
                    }

                    // Album de reportes del usuario
                    composable("vecino_reportes") {
                        AlbumReporteScreen(
                            reporteListViewModel = reporteListViewModel,
                            soloDelUsuario = true,
                            onReporteClick = { reporteId ->
                                //Al tocar un reporte
                                localNavController.navigate("detalle_reporte/$reporteId?editable=editable")
                            }
                        )
                    }

                    // ✅ Detalle de reporte (modo editable o solo lectura)
                    composable(
                        route = "detalle_reporte/{reporteId}?editable={editable}",
                        arguments = listOf(
                            navArgument("reporteId") { type = NavType.StringType },
                            navArgument("editable") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val reporteId = backStackEntry.arguments?.getString("reporteId") ?: ""
                        val editable = backStackEntry.arguments?.getString("editable") ?: ""

                        ReporteDetailScreen(
                            navController = localNavController,
                            reporteId = reporteId,
                            reporteListVM = reporteListViewModel,
                            modo = editable,
                            userViewModel = userViewModel
                        )
                    }
                }

            }
        }
    }
}
