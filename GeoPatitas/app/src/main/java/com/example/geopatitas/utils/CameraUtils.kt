package com.example.geopatitas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {
    fun createImageFile(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "JPEG_${timeStamp}_"
        val storageDir = context.externalCacheDir
        val file = File.createTempFile(fileName, ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Pair(file, uri)
    }
}
