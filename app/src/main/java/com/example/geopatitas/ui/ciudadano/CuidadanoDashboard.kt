package com.example.geopatitas.ui.ciudadano

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadanoDashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val localNavController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("reportar") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Reportar") },
                    selected = selectedRoute == "reportar",
                    onClick = {
                        selectedRoute = "reportar"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("reportar") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Reportar") }
                )

                NavigationDrawerItem(
                    label = { Text("Historial") },
                    selected = selectedRoute == "historial_ciudadano",
                    onClick = {
                        selectedRoute = "historial_ciudadano"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("historial_ciudadano") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Historial") }
                )

                NavigationDrawerItem(
                    label = { Text("Mapa") },
                    selected = selectedRoute == "mapa_ciudadano",
                    onClick = {
                        selectedRoute = "mapa_ciudadano"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("mapa_ciudadano") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Mapa") }
                )

                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = selectedRoute == "perfil_ciudadano",
                    onClick = {
                        selectedRoute = "perfil_ciudadano"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("perfil_ciudadano") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }
                )

                NavigationDrawerItem(
                    label = { Text("Ver ONGs") },
                    selected = selectedRoute == "ver_ongs",
                    onClick = {
                        selectedRoute = "ver_ongs"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("ver_ongs") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Ver ONGs") }
                )

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0)
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
                    title = { Text("Dashboard Ciudadano") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = localNavController,
                    startDestination = "reportar"
                ) {
                    composable("reportar") {
                        ReporteFormScreen(localNavController)
                    }
                    composable("historial_ciudadano") {
                        ReportesRealizadosScreen(localNavController)
                    }
                    composable("mapa_ciudadano") {
                        CiudadanoMapaScreen(localNavController)
                    }
                    composable("perfil_ciudadano") {
                        CiudadanoPerfilScreen(localNavController)
                    }
                    composable("ver_ongs") {
                        OrganizacionesScreen(localNavController)
                    }
                }
            }
        }
    }
}