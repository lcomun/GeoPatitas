package com.example.geopatitas.ui.ong

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
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
fun OrganizacionDashboardScreen(navController: NavController) {
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
                    selected = selectedRoute == "mapa_ong",
                    onClick = {
                        selectedRoute = "mapa_ong"
                        coroutineScope.launch { drawerState.close() }
                        localNavController.navigate("mapa_ong") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Mapa") }
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
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Historial") }
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
                    title = { Text("Dashboard ONG") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Aquí se renderiza la pantalla seleccionada
                NavHost(
                    navController = localNavController,
                    startDestination = "mapa_ong"
                ) {
                    composable("mapa_ong") {
                        // Llama a tu pantalla Mapa ONG
                        OrganizacionMapaScreen(localNavController)
                    }
                    composable("perfil_ong") {
                        OrganizacionPerfilScreen(localNavController)
                    }
                    composable("historial_ong") {
                        ReportesAtendidosScreen(localNavController)
                    }
                }
            }
        }
    }
}