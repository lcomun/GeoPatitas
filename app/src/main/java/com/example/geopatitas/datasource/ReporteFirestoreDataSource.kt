package com.example.geopatitas.datasource

import com.example.geopatitas.model.Reporte
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ReporteFirestoreDataSource {
    private val db = Firebase.firestore
    private val reportsCollection = db.collection("reportes")

    suspend fun saveReport(reporte: Reporte) {
        try {
            reportsCollection.add(reporte).await()
        } catch (e: Exception) {
            println("Error al guardar reporte en Firestore: ${e.message}")
            throw Exception("No se pudo guardar el reporte en la base de datos.", e)
        }
    }

}