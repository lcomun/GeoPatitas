package com.example.geopatitas.ui.screen.comun

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.geopatitas.ui.viewmodel.CurrentUserState
import com.example.geopatitas.ui.viewmodel.ReporteListViewModel2
import com.example.geopatitas.ui.viewmodel.UserViewModel
import com.example.geopatitas.utils.eliminarReportePorId
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.geopatitas.ui.screen.aliado.InfoItem
import com.example.geopatitas.utils.desmarcarReporteComoAtendido

@Composable
fun ReporteDetailScreen(
    navController: NavController,
    reporteId: String,
    userViewModel: UserViewModel,
    modo: String,
    reporteListVM: ReporteListViewModel2
) {
    val context = LocalContext.current
    val currentUserState = userViewModel.currentUserData.collectAsState().value

    val currentUser = userViewModel.currentUserData.collectAsState().value
    val isOrganizacion = (currentUser as? CurrentUserState.Success)?.user?.tipoUsuario == "Aliado"

    LaunchedEffect(Unit) {
        if (reporteListVM.listaReportes.value.isEmpty()) {
            reporteListVM.cargarReportes()
        }
        reporteListVM.cargarReportePorId(reporteId)
    }

    val reporte = reporteListVM.reporteSeleccionado.collectAsState().value

    if (reporte == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Imagen de creación (siempre se muestra si está disponible)
        if (reporte.imagenCreacionUrl.isNotEmpty()) {
            AsyncImage(
                model = reporte.imagenCreacionUrl,
                contentDescription = "Imagen de creación",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
            )
        } else {
            Text("No se encontró imagen de creación.")
        }

        InfoItem("Tipo de animal", reporte.tipoAnimal)
        InfoItem("Apariencia", reporte.aparienciaAnimal)
        InfoItem("Carácter", reporte.caracterAnimal)
// InfoItem("Ubicación", reporte.ubicacion?.direccion ?: "Sin ubicación")
        InfoItem("Estado", reporte.estado)
        InfoItem("Frecuencia de avistamiento", reporte.frecuenciaAvistamiento)
        InfoItem(
            "Fecha de reporte",
            reporteListVM.formatearFecha(reporte.fechaCreacion.toDate())
        )

// Si el estado NO es "No atendido", mostrar todos los campos extra
        if (reporte.estado == "Atendido") {
            Spacer(modifier = Modifier.height(16.dp))

            // InfoItem("ID Organización", reporte.idOrganizacion ?: "N/A")
            InfoItem(
                "Fecha de atención",
                reporte.fechaAtencion?.toDate()?.let { reporteListVM.formatearFecha(it) } ?: "Sin fecha"
            )
            InfoItem("Observaciones", reporte.observaciones ?: "N/A")
            // InfoItem("Ubicación de atención", reporte.ubicacionAtencion?.direccion ?: "Sin ubicación")
            InfoItem("Info adicional", reporte.infoAdicional)

            if (!reporte.imagenAtencionUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = reporte.imagenAtencionUrl,
                    contentDescription = "Imagen de atención",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else {
                InfoItem("Imagen de atención", "No se encontró imagen de atención.")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón "Regresar"
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar")
        }

        val user = (currentUserState as? CurrentUserState.Success)?.user

        // Botón de "Crear reporte" solo para ciudadanos
        if (user != null) {
            if(user.tipoUsuario == "Vecino"){
                if(modo == "editable"){
                    BotonEliminarReporte(
                        idReporte = reporte.id,
                        modifier = Modifier.fillMaxWidth(),
                        onEliminado = {
                            navController.navigate("vecino_reportes") // Vuelve a la pantalla anterior tras eliminar
                        }
                    )
                }
            }else if(user.tipoUsuario == "Aliado"){
                if(reporte.estado == "No atendido" && modo == "atendible"){
                    Button(
                        onClick = { navController.navigate("atender_reporte/${reporte.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Atender")
                    }
                }else if (modo == "editable") {
                    BotonDesatender(
                        idReporte = reporte.id,
                        modifier = Modifier.fillMaxWidth(),
                        onDesatendido = {
                            navController.navigate("aliado_reportes") // Vuelve a la pantalla anterior tras eliminar
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun BotonConfirmacion(
    textoBoton: String,
    tituloDialogo: String,
    mensajeDialogo: String,
    textoConfirmar: String = "Confirmar",
    textoCancelar: String = "Cancelar",
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(tituloDialogo) },
            text = { Text(mensajeDialogo) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onConfirmar()
                }) {
                    Text(textoConfirmar)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(textoCancelar)
                }
            }
        )
    }

    Button(
        onClick = { mostrarDialogo = true },
        modifier = modifier
    ) {
        Text(textoBoton)
    }
}

@Composable
fun BotonEliminarReporte(
    idReporte: String,
    modifier: Modifier = Modifier,
    onEliminado: () -> Unit = {}
) {
    val context = LocalContext.current

    BotonConfirmacion(
        textoBoton = "Borrar",
        tituloDialogo = "Confirmar eliminación",
        mensajeDialogo = "¿Estás seguro de que deseas eliminar este reporte? Esta acción no se puede deshacer.",
        textoConfirmar = "Eliminar",
        onConfirmar = {
            eliminarReportePorId(
                idReporte = idReporte,
                onSuccess = {
                    Toast.makeText(context, "Reporte eliminado con éxito", Toast.LENGTH_SHORT).show()
                    onEliminado()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        },
        modifier = modifier
    )
}

@Composable
fun BotonDesatender(
    idReporte: String,
    modifier: Modifier = Modifier,
    onDesatendido: () -> Unit = {}
) {
    val context = LocalContext.current

    BotonConfirmacion(
        textoBoton = "Desatender",
        tituloDialogo = "Quitar estado 'Atendido'",
        mensajeDialogo = "¿Estás seguro de que deseas marcar este reporte como no atendido?",
        textoConfirmar = "Quitar",
        onConfirmar = {
            desmarcarReporteComoAtendido(
                idReporte = idReporte,
                onSuccess = {
                    Toast.makeText(context, "Reporte marcado como no atendido", Toast.LENGTH_SHORT).show()
                    onDesatendido()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Error al desatender: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        },
        modifier = modifier
    )
}
