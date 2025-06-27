package com.example.geopatitas.utils

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.TimeWindow
import java.io.File

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.cloudinary.android.preprocess.Crop

object CloudinaryUtils {

    private var initialized = false
    fun init(context: Context) {
        if (!initialized) {
            val config: HashMap<String, String> = hashMapOf(
                "cloud_name" to "detw4vtks", // ← Reemplaza con tu info real
                "api_key" to "441694897484395",
                "api_secret" to "ZNvYl362M6BVuJNl4gKVPvMm9Pw"
            )
            MediaManager.init(context, config)
            initialized = true
        }
    }

    @Composable
    fun showImageFromCloudinary(imageUrl: String) {
        // Usar Coil para cargar la imagen directamente desde Cloudinary
        AsyncImage(
            model = imageUrl,  // Solo la URL de la imagen sin transformaciones
            contentDescription = "Descripción de la imagen",  // Descripción para accesibilidad
            modifier = Modifier.size(200.dp)  // Establecer tamaño de la imagen en la UI
        )
    }

    fun uploadImageAndReturnUrl(
        file: File,
        onSuccess: (url: String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        MediaManager.get().upload(file.path)
            .option("resource_type", "auto") // opcional, útil si subes imágenes y videos
            .policy(com.cloudinary.android.policy.UploadPolicy.Builder()
                .maxRetries(2)
                .build())
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Puedes mostrar progreso si quieres
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        onSuccess(url)
                    } else {
                        onError(Exception("No se obtuvo la URL"))
                    }
                }

                override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                    onError(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                    Log.w("Cloudinary", "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }
}