package com.example.geopatitas.utils

import android.util.Log
import androidx.compose.ui.text.TextStyle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.geopatitas.data.Reporte
import com.example.geopatitas.data.Ubicacion
import com.example.geopatitas.data.Usuario
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

fun guardarUsuario(usuario: Usuario, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    val db = Firebase.firestore
    val docRef = db.collection("reportes").document() // Genera ID único

    val usuarioConId = usuario.copy(idUsuario = docRef.id)

    db.collection("usuarios")
        .add(usuarioConId)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

fun guardarReporte(
    reporte: Reporte,
    onSuccess: (String) -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    val docRef = db.collection("reportes").document() // Genera ID único

    val reporteConId = reporte.copy(id = docRef.id)

    docRef.set(reporteConId)
        .addOnSuccessListener {
            onSuccess(docRef.id) // Devolvemos el ID generado
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}


fun verificarUsuarioRegistrado(
    correo: String,
    onResult: (Boolean) -> Unit
) {
    val db = Firebase.firestore
    db.collection("usuarios")
        .whereEqualTo("correo", correo)
        .get()
        .addOnSuccessListener { result ->
            onResult(!result.isEmpty)
        }
        .addOnFailureListener {
            onResult(false) // Por seguridad, si falla asumimos que no está
        }
}

suspend fun obtenerUsuarioDesdeFirestore(correo: String): Usuario? {
    return try {
        val db = Firebase.firestore
        val result = db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .await() // Espera el resultado de forma síncrona

        if (!result.isEmpty) {
            result.documents.first().toObject<Usuario>() // Convierte el documento al objeto Usuario
        } else {
            null // No se encontró ningún usuario con ese correo
        }
    } catch (e: Exception) {
        // Manejar cualquier error que ocurra durante la consulta
        println("Error al obtener usuario desde Firestore: ${e.message}")
        null // Devolver null en caso de error
    }
}

suspend fun obtenerTodosLosReportes(): List<Reporte> {
    return try {
        val db = Firebase.firestore
        val snapshot = db.collection("reportes").get().await()
        val reportes = snapshot.documents.mapNotNull { doc ->
            val reporte = doc.toObject<Reporte>()
            reporte?.copy(id = doc.id)  // Aquí asignamos el id del documento
        }

        Log.d("a","Total de reportes en Firestore: ${reportes.size}")

        // Filtrar reportes con campos obligatorios
        reportes.filter { reporte ->
            reporte.idUsuario != null
        }
        /*
        &&
                    reporte.tipoAnimal.isNotBlank() &&
                    reporte.caracterAnimal.isNotBlank() &&
                    reporte.aparienciaAnimal.isNotBlank() &&
                    reporte.imagenCreacionUrl != null && reporte.imagenCreacionUrl.isNotBlank() &&
                    //(reporte.ubicacion != LatLng(0.0, 0.0)) &&
                    reporte.estado.isNotBlank() &&
                    reporte.fechaCreacion != null &&
                    reporte.frecuenciaAvistamiento.isNotBlank()
         */
    } catch (e: Exception) {
        println("Error al obtener reportes: ${e.message}")
        emptyList()
    }
}

suspend fun obtenerReportePorId(id: String): Reporte? {
    return try {
        val doc = Firebase.firestore.collection("reportes").document(id).get().await()
        if (doc.exists()) {
            doc.toObject<Reporte>()?.copy(id = doc.id)
        } else null
    } catch (e: Exception) {
        Log.e("ReporteViewModel", "Error obteniendo reporte por ID: ${e.message}")
        null
    }
}

suspend fun obtenerReportesPaginados(
    ultimoDoc: DocumentSnapshot?,  // null si es la primera página
    pageSize: Long = 10
): Pair<List<Reporte>, DocumentSnapshot?> {
    return try {
        val db = Firebase.firestore
        val query = if (ultimoDoc == null) {
            db.collection("reportes")
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(pageSize)
        } else {
            db.collection("reportes")
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .startAfter(ultimoDoc)
                .limit(pageSize)
        }

        val snapshot = query.get().await()
        val reportes = snapshot.documents.mapNotNull { doc ->
            doc.toObject<Reporte>()?.copy(id = doc.id)
        }

        // Retornamos la lista y el último documento como cursor para la próxima carga
        val lastDoc = snapshot.documents.lastOrNull()
        Pair(reportes, lastDoc)

    } catch (e: Exception) {
        Log.e("Firestore", "Error en paginación: ${e.message}")
        Pair(emptyList(), null)
    }
}

fun atenderReporte(
    idReporte: String,
    idOrganizacion: String,
    observaciones: String,
    imagenAtencionUrl: String,
    userLocation: LatLng,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    val reporteRef = db.collection("reportes").document(idReporte)

    // Creamos un mapa con los datos a actualizar
    val datosActualizados = mapOf(
        "estado" to "Atendido",
        "idOrganizacion" to idOrganizacion,
        "fechaAtencion" to Timestamp.now(),
        "observaciones" to observaciones,
        "imagenAtencionUrl" to imagenAtencionUrl,
        "ubicacionAtencion" to Ubicacion(userLocation)
    )

    // Actualizamos el documento en Firestore
    reporteRef.update(datosActualizados)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun eliminarReportePorId(
    idReporte: String,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    val docRef = db.collection("reportes").document(idReporte)

    docRef.delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Reporte eliminado correctamente: $idReporte")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al eliminar el reporte: ${e.message}")
            onFailure(e)
        }
}

fun desmarcarReporteComoAtendido(
    idReporte: String,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    val reporteRef = db.collection("reportes").document(idReporte)

    val datosActualizados = mapOf(
        "estado" to "No atendido",
        "idOrganizacion" to null,
        "fechaAtencion" to null,
        "observaciones" to null,
        "imagenAtencionUrl" to null,
        "ubicacionAtencion" to null
    )

    reporteRef.update(datosActualizados)
        .addOnSuccessListener {
            Log.d("Firestore", "Reporte marcado como 'No atendido': $idReporte")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al desmarcar reporte como atendido: ${e.message}")
            onFailure(e)
        }
}


suspend fun obtenerResumenAtencion(): Pair<Pair<String, Int>, Pair<String, Int>> {
    val reportes = obtenerTodosLosReportes()
    val atendidos = reportes.count { it.estado == "Atendido" }
    val pendientes = reportes.size - atendidos

    return Pair(Pair("Atendidos", atendidos), Pair("Pendientes", pendientes))
}


suspend fun obtenerReportePorTipoAnimal(): Map<String, Int> {
    val reportes = obtenerTodosLosReportes()
    return reportes.groupingBy { it.tipoAnimal.ifBlank { "Desconocido" } }
        .eachCount()
}

suspend fun obtenerReportesPorMes(): Map<String, Int> {
    val reportes = obtenerTodosLosReportes()

    val formatoMesAnio = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    return reportes.mapNotNull { reporte ->
        try {
            val date = reporte.fechaCreacion?.toDate()
            if (date != null) {
                formatoMesAnio.format(date)  // Ej: "julio 2025"
            } else null
        } catch (e: Exception) {
            null
        }
    }
        .groupingBy { it }
        .eachCount()
        .toSortedMap()
}