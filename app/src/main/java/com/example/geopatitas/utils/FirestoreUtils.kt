package com.example.geopatitas.utils

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.geopatitas.model.Reporte
import com.example.geopatitas.model.Ubicacion
import com.example.geopatitas.model.Usuario
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

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
