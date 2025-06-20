package com.example.geopatitas.ui.ciudadano

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geopatitas.model.Reporte
import com.example.geopatitas.utils.LocationUtils
import com.example.geopatitas.utils.guardarReporte

@Composable
fun ReporteFormScreen(navController: NavController) {
    val context = LocalContext.current

    // Variables para el formulario
    var tipoAnimal by remember { mutableStateOf("Perro") }
    var caracter by remember { mutableStateOf("Agresivo") }
    var apariencia by remember { mutableStateOf("Saludable") }

    // Estado para ubicación
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        LocationUtils.initialize(context)

        // Obtener la última ubicación conocida
        LocationUtils.getLastLocation(context) { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }

        // Suscribirse a actualizaciones en tiempo real
        LocationUtils.startLocationUpdates(context) { location ->
            latitude = location.latitude
            longitude = location.longitude
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pantalla de Reporte de Perritos", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Ubicación: Lat: $latitude, Long: $longitude", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Tipo de Animal
        Text("Tipo de Animal:", style = MaterialTheme.typography.bodyLarge)
        SelectorDropdown(
            selected = tipoAnimal,
            options = listOf("Perro", "Gato", "Otro"),
            onSelected = { tipoAnimal = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Características
        Text("Características:", style = MaterialTheme.typography.bodyLarge)
        SelectorDropdown(
            selected = caracter,
            options = listOf("Agresivo", "Cariñoso", "Miedoso", "Pacífico"),
            onSelected = { caracter = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apariencia
        Text("Apariencia:", style = MaterialTheme.typography.bodyLarge)
        SelectorDropdown(
            selected = apariencia,
            options = listOf("Saludable", "Con Heridas Leves", "Con Heridas Graves", "Muy Delgado", "Con Sobrepeso"),
            onSelected = { apariencia = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para enviar reporte
        Button(
            onClick = {
                val reporte = Reporte(
                    tipoAnimal = tipoAnimal,
                    caracterAnimal = caracter,
                    aparienciaAnimal = apariencia,
                    ubicacion = mapOf("latitude" to latitude, "longitude" to longitude),
                    fechaCreacion = com.google.firebase.Timestamp.now()
                )
                guardarReporte(reporte)
                Toast.makeText(context, "Reporte Enviado", Toast.LENGTH_SHORT).show()
                // Navegar a la pantalla de mapa
                navController.navigate("mapa_ciudadano") // Aquí navegas al mapa después de guardar el reporte
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar Reporte")
        }
    }
}

@Composable
fun SelectorDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selected)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}