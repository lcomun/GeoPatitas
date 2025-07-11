package com.example.geopatitas.ui.screen.vecino

import android.Manifest
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel // Importar para usar viewModel()
import coil.compose.AsyncImage
import com.example.geopatitas.ui.screen.comun.SelectorDropdown
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import com.example.geopatitas.utils.CameraUtils
import com.example.geopatitas.utils.CloudinaryUtils
import com.example.geopatitas.utils.LocationUtils
import com.example.geopatitas.ui.viewmodel.LocationViewModel
import com.example.geopatitas.ui.viewmodel.ReporteFormViewModel
import com.example.geopatitas.ui.viewmodel.UiEvent
import com.google.android.gms.maps.model.LatLng

@Composable
fun ReporteFormScreen(navController: NavController, userId: String, locationVM: LocationViewModel) {
    val context = LocalContext.current
    val viewModel: ReporteFormViewModel = viewModel()

    var permissionStep by remember { mutableStateOf(0) }
    val _userLocation by locationVM.ubicacion.collectAsState()

    val userLocation = LatLng(
        _userLocation?.latitude ?: 0.0,
        _userLocation?.longitude ?: 0.0
    )

    LaunchedEffect(Unit) {
        //CloudinaryUtils.init(context)
        //LocationUtils.initialize(context)
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

    // Define el launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.photoFile = photoFile
        viewModel.onPhotoResult(success)
    }

    PermissionRequester(
        locationVM = locationVM,
        setStep = { permissionStep = it },
        onGranted = { /* permisos concedidos: no hacer nada más aquí, ya estamos con step=2 */ },
        onDenied = {
            Toast.makeText(context, "Permisos requeridos para continuar", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    )

    if (permissionStep < 2) {
        FullScreenLoading("Solicitando permisos...")
        return
    }

    FormContent(
        userLocation = userLocation,
        viewModel = viewModel,
        photoUri = photoUri,
        cameraLauncher = cameraLauncher,
        userId = userId,
        navController = navController
    )
}

@Composable
fun FullScreenLoading(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
fun PermissionRequester(
    locationVM: LocationViewModel,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    setStep: (Int) -> Unit
) {
    val context = LocalContext.current

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            setStep(2)
            LocationUtils.startLocationUpdates { locationVM.setUbicacion(it) }
            onGranted()
        } else {
            Toast.makeText(context, "Permiso de ubicación requerido", Toast.LENGTH_SHORT).show()
            onDenied()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            setStep(1)
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            Toast.makeText(context, "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
            onDenied()
        }
    }

    LaunchedEffect(Unit) {
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        val locationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (cameraGranted && locationGranted) {
            setStep(2)
            LocationUtils.startLocationUpdates { locationVM.setUbicacion(it) }
            onGranted()
        } else if (!cameraGranted) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        } else {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@Composable
fun FormContent(
    userLocation: LatLng,
    viewModel: ReporteFormViewModel,
    photoUri: Uri,
    cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    userId: String,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "¡Animalito encontrado!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        /*
        Text(
            text = "Ubicación actual:\nLatitud: ${"%.6f".format(userLocation.latitude)}\nLongitud: ${
                "%.6f".format(
                    userLocation.longitude
                )
            }",
            style = MaterialTheme.typography.bodyMedium
        )
         */
        if (viewModel.showFormValidationErrors) {
            Text(
                "La ubicación no se ha podido obtener. Asegúrate de tener los servicios de ubicación activados y una buena señal.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tipo de Animal
        SelectorDropdown(
            label = "Tipo de animal",
            selectedOption = viewModel.tipoAnimal,
            options = listOf("Perro", "Gato", "Otro"),
            onOptionSelected = { viewModel.tipoAnimal = it }
        )
        if (viewModel.showFormValidationErrors && (viewModel.tipoAnimal.isBlank() || viewModel.tipoAnimal == "Perro")) {
            Text(
                "Selecciona el tipo de animal.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Características
        SelectorDropdown(
            label = "Carácter",
            selectedOption = viewModel.caracter,
            options = listOf("Agresivo", "Cariñoso", "Miedoso", "Pacífico"),
            onOptionSelected = { viewModel.caracter = it }
        )
        if (viewModel.showFormValidationErrors && (viewModel.caracter.isBlank() || viewModel.caracter == "Agresivo")) {
            Text(
                "Selecciona el carácter del animal.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Apariencia
        SelectorDropdown(
            label = "Apariencia",
            selectedOption = viewModel.apariencia,
            options = listOf(
                "Saludable",
                "Con Heridas Leves",
                "Con Heridas Graves",
                "Muy Delgado",
                "Con Sobrepeso"
            ),
            onOptionSelected = { viewModel.apariencia = it }
        )
        if (viewModel.showFormValidationErrors && (viewModel.apariencia.isBlank() || viewModel.apariencia == "Saludable")) {
            Text(
                "Selecciona la apariencia del animal.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Frecuencia de avistamiento
        SelectorDropdown(
            label = "Frecuencia de avistamiento",
            selectedOption = viewModel.frecuenciaAvistamiento,
            options = listOf(
                "Siempre está aquí",
                "Primera vez que lo veo",
                "Viene de vez en cuando"
            ),
            onOptionSelected = { viewModel.frecuenciaAvistamiento = it }
        )
        if (viewModel.showFormValidationErrors && (viewModel.frecuenciaAvistamiento.isBlank() || viewModel.frecuenciaAvistamiento == "Siempre está aquí")) {
            Text(
                "Selecciona la frecuencia de avistamiento.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Información extra (Opcional)
        Text(
            "Información extra (Opcional)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        TextField(
            value = viewModel.infoExtra,
            onValueChange = { newText -> viewModel.infoExtra = newText },
            placeholder = { Text("Escribe detalles adicionales...") },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar la foto si existe
        if (viewModel.photoTaken) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto del animal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.resetPhoto() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar imagen")
                }
            }
        }

        if (viewModel.showFormValidationErrors && !viewModel.photoTaken) {
            Text(
                "Es obligatorio tomar una foto del animal.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.photoError) {
            Text(
                "Hubo un error al tomar la foto. Por favor, intente de nuevo.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { cameraLauncher.launch(photoUri) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reintentar")
            }
        } else if (!viewModel.photoTaken) {
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
                viewModel.enviarReporte(
                    idUsuario = userId,
                    userLocation = userLocation
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar Reporte")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("reportes") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar")
        }
    }
}