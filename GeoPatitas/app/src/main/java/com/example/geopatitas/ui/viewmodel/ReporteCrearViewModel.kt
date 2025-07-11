package com.example.geopatitas.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.data.Reporte
import com.example.geopatitas.data.Ubicacion
import com.example.geopatitas.utils.CloudinaryUtils
import com.example.geopatitas.utils.guardarReporte
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log


// UiEvent.kt
sealed class UiEvent {
    data class ShowToast(val message: String): UiEvent()
    object NavigateBack: UiEvent()
    // Puedes agregar más eventos aquí
}

class ReporteFormViewModel : ViewModel() {

    var tipoAnimal by mutableStateOf("Perro")
    var caracter by mutableStateOf("Agresivo")
    var apariencia by mutableStateOf("Saludable")
    var frecuenciaAvistamiento by mutableStateOf("Siempre está aquí")
    var infoExtra by mutableStateOf("")

    var photoFile: File? by mutableStateOf(null)
    var photoTaken by mutableStateOf(false)
    var photoError by mutableStateOf(false)

    var showFormValidationErrors by mutableStateOf(false)

    // Channel para enviar eventos UI a la Screen
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun validateForm(): Boolean {
        if (tipoAnimal.isBlank()) return false
        if (caracter.isBlank()) return false
        if (apariencia.isBlank()) return false
        if (frecuenciaAvistamiento.isBlank()) return false
        if (!photoTaken || photoFile == null) return false
        return true
    }

    fun onPhotoResult(success: Boolean) {
        photoTaken = success
        photoError = !success
        if (!success) {
            sendUiEvent(UiEvent.ShowToast("No se pudo tomar la foto. Por favor, inténtelo de nuevo."))
        }
    }

    fun resetPhoto() {
        photoFile = null
        photoTaken = false
        photoError = false
    }

    fun enviarReporte2(idUsuario: String, userLocation: LatLng) {

        if (!validateForm()) {
            showFormValidationErrors = true
            sendUiEvent(UiEvent.ShowToast("Por favor, completa todos los campos obligatorios y toma la foto."))
            return
        }

        showFormValidationErrors = false

        viewModelScope.launch {
            // Simular una imagen subida usando una URL fija
            val imageUrl = "https://res.cloudinary.com/detw4vtks/image/upload/v1750883582/uzly6msteppjebi1t1ag.jpg"

            val reporte = Reporte(
                idUsuario = idUsuario,
                tipoAnimal = tipoAnimal,
                caracterAnimal = caracter,
                aparienciaAnimal = apariencia,
                imagenCreacionUrl = imageUrl,
                ubicacion = Ubicacion(userLocation),
                fechaCreacion = Timestamp.now(),
                frecuenciaAvistamiento = frecuenciaAvistamiento,
                infoAdicional = infoExtra
            )

            guardarReporte(
                reporte = reporte,
                onSuccess = { idGenerado ->
                    sendUiEvent(UiEvent.ShowToast("Reporte enviado con éxito."))
                    sendUiEvent(UiEvent.NavigateBack)
                },
                onFailure = {
                    sendUiEvent(UiEvent.ShowToast("Error al guardar el reporte."))
                }
            )
        }
    }

    fun enviarReporte(idUsuario: String, userLocation: LatLng) {
        Log.d("A", "" + userLocation)

        if (!validateForm()) {
            showFormValidationErrors = true
            sendUiEvent(UiEvent.ShowToast("Por favor, completa todos los campos obligatorios y toma la foto."))
            return
        }

        showFormValidationErrors = false

        viewModelScope.launch {
            photoFile?.let { file ->
                CloudinaryUtils.uploadImageAndReturnUrl(
                    file = file,
                    onSuccess = { imageUrl ->
                        val reporte = Reporte(
                            idUsuario = idUsuario,
                            tipoAnimal = tipoAnimal,
                            caracterAnimal = caracter,
                            aparienciaAnimal = apariencia,
                            imagenCreacionUrl = imageUrl,
                            ubicacion = Ubicacion(userLocation),
                            fechaCreacion = Timestamp.now(),
                            frecuenciaAvistamiento = frecuenciaAvistamiento,
                            infoAdicional = infoExtra
                        )
                        guardarReporte(
                            reporte = reporte,
                            onSuccess = { idGenerado ->
                                sendUiEvent(UiEvent.ShowToast("Reporte enviado con éxito."))
                                sendUiEvent(UiEvent.NavigateBack)
                            },
                            onFailure = {
                                sendUiEvent(UiEvent.ShowToast("Error al guardar el reporte."))
                            }
                        )
                    },
                    onError = {
                        sendUiEvent(UiEvent.ShowToast("Error al subir imagen. Intente de nuevo."))
                    }
                )
            } ?: run {
                sendUiEvent(UiEvent.ShowToast("Error interno: La foto no está disponible."))
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
