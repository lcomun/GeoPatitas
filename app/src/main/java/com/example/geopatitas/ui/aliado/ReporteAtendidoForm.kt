package com.example.geopatitas.ui.aliado

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.geopatitas.model.Reporte
import com.example.geopatitas.ui.vecino.FullScreenLoading
import com.example.geopatitas.ui.vecino.PermissionRequester
import com.example.geopatitas.utils.CameraUtils
import com.example.geopatitas.utils.CloudinaryUtils
import com.example.geopatitas.utils.LocationUtils
import com.example.geopatitas.viewmodel.CurrentUserState
import com.example.geopatitas.viewmodel.LocationViewModel
import com.example.geopatitas.viewmodel.ReporteAtederViewModel
import com.example.geopatitas.viewmodel.ReporteListViewModel2
import com.example.geopatitas.viewmodel.UiEvent
import com.example.geopatitas.viewmodel.UserViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun ReporteAtendidoFormScreen(
    navController: NavController,
    reporteId: String,
    userViewModel: UserViewModel,
    locationVM: LocationViewModel
) {
    val context = LocalContext.current
    val reporteViewModel: ReporteListViewModel2 = viewModel()
    val viewModel: ReporteAtederViewModel = viewModel()

    var permissionStep by remember { mutableStateOf(0) }
    val _userLocation by locationVM.ubicacion.collectAsState()

    val userLocation = LatLng(
        _userLocation?.latitude ?: 0.0,
        _userLocation?.longitude ?: 0.0
    )

    val currentUser = userViewModel.currentUserData.collectAsState().value
    val idOrganizacion = (currentUser as? CurrentUserState.Success)?.user?.idUsuario ?: ""

    LaunchedEffect(Unit) {
        CloudinaryUtils.init(context)
        LocationUtils.initialize(context)
        reporteViewModel.cargarReportePorId(reporteId)
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                UiEvent.NavigateBack -> {
                    navController.navigate("reportes")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            LocationUtils.stopLocationUpdates()
        }
    }

    val (photoFile, photoUri) = remember {
        CameraUtils.createImageFile(context)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.photoFile = photoFile
        viewModel.onPhotoResult(success)
    }

    PermissionRequester(
        locationVM = locationVM,
        setStep = { permissionStep = it },
        onGranted = {},
        onDenied = {
            Toast.makeText(context, "Permisos requeridos para continuar", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    )

    if (permissionStep < 2) {
        FullScreenLoading("Solicitando permisos...")
        return
    }

    val reporte = reporteViewModel.reporteSeleccionado.collectAsState().value

    if (reporte == null) {
        FullScreenLoading("Cargando reporte...")
        return
    }

    AtenderReporteContent(
        reporte = reporte,
        viewModel = viewModel,
        photoUri = photoUri,
        cameraLauncher = cameraLauncher,
        userLocation = userLocation,
        idOrganizacion = idOrganizacion,
        navController = navController
    )
}

@Composable
fun AtenderReporteContent(
    reporte: Reporte,
    viewModel: ReporteAtederViewModel,
    photoUri: Uri,
    cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    userLocation: LatLng,
    idOrganizacion: String,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val notasAtencion = remember { mutableStateOf(reporte.observaciones ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Atender Reporte",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("ID del reporte: ${reporte.id}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Estado actual: ${reporte.estado}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Descripción: ${reporte.infoAdicional}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = notasAtencion.value,
            onValueChange = { notasAtencion.value = it },
            label = { Text("Notas de atención") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.photoTaken) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto de atención",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.resetPhoto() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Eliminar imagen")
                }
            }
        } else {
            Button(
                onClick = { cameraLauncher.launch(photoUri) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar Foto")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.atenderReporteVm(
                    idReporte = reporte.id,
                    idOrganizacion = idOrganizacion,
                    observaciones = notasAtencion.value,
                    userLocation = userLocation
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar atención")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}
