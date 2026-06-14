package com.algo1127.weather.utils

import android.annotation.SuppressLint
import android.content.Context
import com.algo1127.weather.data.network.AemetMunicipality
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

object LocationHelper {

    /**
     * Obtiene las coordenadas GPS actuales.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = Tasks.await(fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Encuentra el municipio de AEMET más cercano a unas coordenadas GPS.
     */
    fun findClosestMunicipality(
        targetLat: Double,
        targetLon: Double,
        municipalities: List<AemetMunicipality>
    ): AemetMunicipality? {

        var closest: AemetMunicipality? = null
        var minDistance = Double.MAX_VALUE

        for (muni in municipalities) {
            // Priority 1: Use decimal coordinates if provided in the JSON
            // Priority 2: Parse AEMET's DMS format if decimal is missing
            val muniLat = muni.latitudDec?.toDoubleOrNull() ?: parseAemetCoordinate(muni.latitud) ?: continue
            val muniLon = muni.longitudDec?.toDoubleOrNull() ?: parseAemetCoordinate(muni.longitud) ?: continue

            val distance = haversine(targetLat, targetLon, muniLat, muniLon)

            if (distance < minDistance) {
                minDistance = distance
                closest = muni
            }
        }
        return closest
    }

    /**
     * Convierte el formato de coordenadas de AEMET a decimal.
     * Soporta tanto "364312N" (DMS) como "-4.43956" (Decimal).
     */
    private fun parseAemetCoordinate(coord: String?): Double? {
        if (coord == null || coord.isBlank()) return null
        
        val cleanCoord = coord.trim().uppercase()
        
        // 1. Try simple decimal first (many municipalities in the JSON use this now)
        cleanCoord.toDoubleOrNull()?.let { return it }

        // 2. Handle AEMET's DMS format: DDMMSSN or DDDMMSSW
        return try {
            val direction = cleanCoord.last()
            val numericPart = cleanCoord.substring(0, cleanCoord.length - 1).filter { it.isDigit() }
            
            if (numericPart.length < 4) return null

            val seconds = numericPart.takeLast(2).toDouble()
            val minutes = numericPart.substring(numericPart.length - 4, numericPart.length - 2).toDouble()
            val degrees = numericPart.substring(0, numericPart.length - 4).toDouble()

            var decimal = degrees + (minutes / 60.0) + (seconds / 3600.0)
            if (direction == 'S' || direction == 'W') {
                decimal = -decimal
            }
            decimal
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fórmula de Haversine para calcular distancia en KM entre dos puntos de la Tierra.
     */
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la tierra en KM
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}