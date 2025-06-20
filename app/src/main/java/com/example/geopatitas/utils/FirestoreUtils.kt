package com.example.geopatitas.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.geopatitas.model.Reporte

fun guardarReporte(reporte: Reporte) {
    val db = Firebase.firestore
    db.collection("reportes")
        .add(reporte)
        .addOnSuccessListener {
            // Manejar éxito
        }
        .addOnFailureListener {
            // Manejar error
        }
}
