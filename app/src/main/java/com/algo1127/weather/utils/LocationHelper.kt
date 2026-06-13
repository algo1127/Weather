package com.algo1127.weather.utils

import com.algo1127.weather.data.network.AemetMunicipality
import kotlin.math.*

object LocationHelper {

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
            val muniLat = muni.latitud?.toDoubleOrNull() ?: continue
            val muniLon = muni.longitud?.toDoubleOrNull() ?: continue

            val distance = haversine(targetLat, targetLon, muniLat, muniLon)

            if (distance < minDistance) {
                minDistance = distance
                closest = muni
            }
        }
        return closest
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