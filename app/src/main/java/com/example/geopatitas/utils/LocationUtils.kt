package com.example.geopatitas.utils

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

object LocationUtils {

    private const val TAG = "LocationUtils"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private var currentLocation: Location? = null

    private var onLocationUpdate: ((Location) -> Unit)? = null

    fun initialize(context: Context) {
        // Inicializar FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        // Crear la LocationRequest
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(60)
        )
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(2))
            .build()
    }

    /**
     * Solicita la última ubicación conocida
     */
    fun getLastLocation(context: Context, callback: (Location?) -> Unit) {
        try {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    callback(location)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting last location: $e")
                    callback(null)
                }
        } catch (securityException: SecurityException) {
            Log.e(TAG, "No location permission: $securityException")
            callback(null)
        }
    }

    /**
     * Comienza a recibir actualizaciones de ubicación
     */
    fun startLocationUpdates(context: Context, onUpdate: (Location) -> Unit) {
        onLocationUpdate = onUpdate

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    currentLocation = location
                    onLocationUpdate?.invoke(location)
                    Log.d(TAG, "New location: $location")
                } else {
                    Log.d(TAG, "Location is null")
                }
            }
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Started location updates.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permission: $e")
        }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Location updates stopped.")
                    } else {
                        Log.d(TAG, "Failed to stop location updates.")
                    }
                }
        }
    }

    fun getCurrentLocation(): Location? = currentLocation
}