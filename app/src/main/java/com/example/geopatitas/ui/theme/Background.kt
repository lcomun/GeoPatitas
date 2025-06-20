package com.example.geopatitas.ui.theme

// File: ui/theme/Background.kt

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun AppBackground(
    imageUrl: String,
    overlayAlpha: Float = 0.4f, // Puedes cambiar esto dinámicamente
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay con opacidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )

        // Contenido encima
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}