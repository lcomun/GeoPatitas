package com.example.geopatitas.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geopatitas.data.Contacto
import com.example.geopatitas.data.Usuario
import com.example.geopatitas.navegation.AppDestinations
import com.example.geopatitas.utils.guardarUsuario

@Composable
fun SignUpScreen(navController: NavController, nombreInicial: String, correo: String) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(nombreInicial) }
    var tipoUsuario by remember { mutableStateOf("Ciudadano") }

    // Solo para Organización
    var contactoNombre by remember { mutableStateOf("") }
    var contactoTipo by remember { mutableStateOf("Email") }
    var contactoValor by remember { mutableStateOf(correo) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Completa tu perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Correo: $correo", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tipo de Usuario:")
        Row {
            RadioButton(
                selected = tipoUsuario == "Vecino",
                onClick = { tipoUsuario = "Vecino" }
            )
            Text("Vecino", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = tipoUsuario == "Aliado",
                onClick = { tipoUsuario = "Aliado" }
            )
            Text("Aliado")
        }

        if (tipoUsuario == "Aliado") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Contacto Obligatorio")

            OutlinedTextField(
                value = contactoNombre,
                onValueChange = { contactoNombre = it },
                label = { Text("Nombre del contacto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contactoValor,
                onValueChange = { contactoValor = it },
                label = { Text("Email o Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val contactos = if (tipoUsuario == "Aliado") {
                    if (contactoValor.isBlank()) {
                        Toast.makeText(context, "Debe ingresar un contacto válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    listOf(
                        Contacto(
                            nombre = contactoNombre.ifBlank { "Contacto principal" },
                            tipo = contactoTipo,
                            valor = contactoValor
                        )
                    )
                } else {
                    listOf(
                        Contacto(
                            nombre = "Correo",
                            tipo = "Email",
                            valor = correo
                        )
                    )
                }

                val nuevoUsuario = Usuario(
                    nombre = nombre,
                    correo = correo,
                    tipoUsuario = tipoUsuario,
                    infoContacto = contactos
                )

                guardarUsuario(
                    usuario = nuevoUsuario,
                    onSuccess = {
                        Toast.makeText(context, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        navController.navigate(
                            if (tipoUsuario == "Vecino") AppDestinations.VecinoFlow.DASHBOARD else AppDestinations.ORGANIZACION_DASHBOARD_ROUTE
                        ) {
                            popUpTo(AppDestinations.CREAR_CUENTA_ROUTE) { inclusive = true }
                        }
                    },
                    onFailure = {
                        Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }
    }
}