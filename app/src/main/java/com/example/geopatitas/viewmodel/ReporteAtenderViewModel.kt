package com.example.geopatitas.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geopatitas.model.Reporte
import com.example.geopatitas.utils.CloudinaryUtils
import com.example.geopatitas.utils.atenderReporte
import com.example.geopatitas.utils.guardarReporte
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class ReporteAtederViewModel : ViewModel() {
    var photoFile: File? by mutableStateOf(null)
    var photoTaken by mutableStateOf(false)
    var photoError by mutableStateOf(false)

    var showFormValidationErrors by mutableStateOf(false)

    // Channel para enviar eventos UI a la Screen
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun validateForm(): Boolean {
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

    fun atenderReporteVm(
        idReporte: String,
        idOrganizacion: String,
        observaciones: String,
        userLocation: LatLng
    ) {
        viewModelScope.launch {
            photoFile?.let { file ->
                CloudinaryUtils.uploadImageAndReturnUrl(
                    file = file,
                    onSuccess = { imageUrl ->
                        atenderReporte(
                            idReporte = idReporte,
                            idOrganizacion = idOrganizacion,
                            observaciones = observaciones,
                            imagenAtencionUrl = imageUrl,
                            userLocation = userLocation
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

        /*
        viewModelScope.launch {
            com.example.geopatitas.utils.atenderReporte(
                idReporte = idReporte,
                idOrganizacion = idOrganizacion,
                observaciones = observaciones,
                imagenAtencionUrl = imagenAtencionUrl,
                ubicacionAtencion = userLocation,
                onSuccess = {sendUiEvent(UiEvent.ShowToast("Reporte atendido"))
                },
                onFailure = { sendUiEvent(UiEvent.ShowToast("Error al subir imagen. Intente de nuevo."))
                }
            )
        }
         */
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
