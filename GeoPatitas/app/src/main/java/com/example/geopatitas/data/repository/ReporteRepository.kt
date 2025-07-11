package com.example.geopatitas.data.repository

import android.util.Log
import com.example.geopatitas.data.Reporte
import com.example.geopatitas.data.Ubicacion
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ReporteRepository {

    private val db = Firebase.firestore
    private val reportesRef = db.collection("reportes")

    suspend fun obtenerTodos(): List<Reporte> {
        return try {
            val snapshot = reportesRef.get().await()
            val reportes = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Reporte>()?.copy(id = doc.id)
            }
            reportes.filter { it.idUsuario != null }
        } catch (e: Exception) {
            Log.e("ReporteRepository", "Error al obtener reportes: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerPorId(id: String): Reporte? {
        return try {
            val doc = reportesRef.document(id).get().await()
            if (doc.exists()) {
                doc.toObject<Reporte>()?.copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            Log.e("ReporteRepository", "Error obteniendo reporte por ID: ${e.message}")
            null
        }
    }

    suspend fun obtenerPaginados(
        ultimoDoc: DocumentSnapshot?,
        pageSize: Long = 10
    ): Pair<List<Reporte>, DocumentSnapshot?> {
        return try {
            val query = if (ultimoDoc == null) {
                reportesRef
                    .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(pageSize)
            } else {
                reportesRef
                    .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .startAfter(ultimoDoc)
                    .limit(pageSize)
            }

            val snapshot = query.get().await()
            val reportes = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Reporte>()?.copy(id = doc.id)
            }

            Pair(reportes, snapshot.documents.lastOrNull())

        } catch (e: Exception) {
            Log.e("ReporteRepository", "Error en paginación: ${e.message}")
            Pair(emptyList(), null)
        }
    }

    suspend fun guardar(reporte: Reporte): String? {
        return try {
            val docRef = reportesRef.document()
            val reporteConId = reporte.copy(id = docRef.id)
            docRef.set(reporteConId).await()
            docRef.id
        } catch (e: Exception) {
            Log.e("ReporteRepository", "Error al guardar reporte: ${e.message}")
            null
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
        val datosActualizados = mapOf(
            "estado" to "Atendido",
            "idOrganizacion" to idOrganizacion,
            "fechaAtencion" to Timestamp.now(),
            "observaciones" to observaciones,
            "imagenAtencionUrl" to imagenAtencionUrl,
            "ubicacionAtencion" to Ubicacion(userLocation)
        )

        reportesRef.document(idReporte)
            .update(datosActualizados)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
