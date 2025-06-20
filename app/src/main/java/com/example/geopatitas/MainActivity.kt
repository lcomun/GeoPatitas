package com.example.geopatitas

import android.os.Bundle
import android.util.Log
import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geopatitas.ui.ciudadano.CiudadanoDashboardScreen
import com.example.geopatitas.ui.ciudadano.CiudadanoMapaScreen
import com.example.geopatitas.ui.ciudadano.CiudadanoPerfilScreen
import com.example.geopatitas.ui.ciudadano.OrganizacionesScreen
import com.example.geopatitas.ui.ciudadano.ReporteFormScreen
import com.example.geopatitas.ui.ciudadano.ReportesRealizadosScreen
import com.example.geopatitas.ui.comun.LoginScreen
import com.example.geopatitas.ui.ong.OrganizacionDashboardScreen
import com.example.geopatitas.ui.theme.GeoPatitasTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class MainActivity : ComponentActivity() {

    //private lateinit var db: FirebaseFirestore
    //private val TAG = "FirestoreCRUD"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //GPS------------------------------------------------------
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }
    //------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //GPS
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            //FirestoreCRUDApp(db)
            GeoPatitasTheme {
                AppNavigation()
            }
        }
    }

    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // Usamos el padding obtenido de Scaffold para pasarlo a los composables
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding) // Aplica el padding aquí
            ) {

                // Pantalla de login
                composable("login") {
                    LoginScreen(navController)
                }

                // Dashboard y pantallas del ciudadano
                composable("dashboard_ciudadano") {
                    CiudadanoDashboardScreen(navController = navController)
                }
                composable("reportar") { ReporteFormScreen(navController) }
                composable("perfil_ciudadano") { CiudadanoPerfilScreen(navController) }
                composable("historial_ciudadano") { ReportesRealizadosScreen(navController) }
                composable("ver_ongs") { OrganizacionesScreen(navController) }
                composable("mapa_ciudadano") { CiudadanoMapaScreen(navController) }

                // Pantallas de la ONG
                composable("dashboard_ong") { OrganizacionDashboardScreen(navController) }
                //composable("mapa_ong") { MapaOngScreen(navController) }
                //composable("perfil_ong") { PerfilOngScreen(navController) }
                //composable("historial_ong") { HistorialOngScreen(navController) }
            }
        }
    }

}