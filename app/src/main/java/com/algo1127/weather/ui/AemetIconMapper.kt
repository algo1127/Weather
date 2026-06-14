package com.algo1127.weather.ui

import androidx.annotation.RawRes
import com.algo1127.weather.R

object AemetIconMapper {

    /**
     * Maps AEMET sky codes to your animated Lottie JSON files.
     * Based on official AEMET weather codes.
     */
    @RawRes
    fun getWeatherIcon(aemetCode: String?, description: String?): Int {
        if (aemetCode.isNullOrBlank()) return R.raw.not_available

        // Check if it's night (AEMET adds an 'n' suffix, e.g., "12n")
        val isNight = aemetCode.endsWith("n", ignoreCase = true)

        // Extract the base number (e.g., "12n" -> 12)
        val baseCode = aemetCode.replace("n", "").toIntOrNull() ?: return R.raw.not_available

        // Check description for additional context
        val descLower = description?.lowercase() ?: ""
        val hasRain = descLower.contains("lluvia") || descLower.contains("precip")
        val hasSnow = descLower.contains("nieve")
        val hasThunder = descLower.contains("tormenta") || descLower.contains("thunder")
        val hasHail = descLower.contains("granizo") || descLower.contains("hail")
        val hasFog = descLower.contains("niebla") || descLower.contains("fog")
        val hasHaze = descLower.contains("calima") || descLower.contains("haze") || descLower.contains("bruma")
        val isExtreme = descLower.contains("fuerte") || descLower.contains("intensa") || descLower.contains("extreme") || descLower.contains("persistente")

        return when (baseCode) {
            // ☀️ CLEAR SKY (11)
            11 -> if (isNight) R.raw.clear_night else R.raw.clear_day

            // ⛅ PARTLY CLOUDY (12, 13)
            12, 13 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night else R.raw.thunderstorms_day
                hasRain && isExtreme -> if (isNight) R.raw.extreme_night_rain else R.raw.extreme_day_rain
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.partly_cloudy_night_drizzle else R.raw.partly_cloudy_day_drizzle
                hasRain -> if (isNight) R.raw.partly_cloudy_night_rain else R.raw.partly_cloudy_day_rain
                hasSnow && isExtreme -> if (isNight) R.raw.extreme_night_snow else R.raw.extreme_day_snow
                hasSnow -> if (isNight) R.raw.partly_cloudy_night_snow else R.raw.partly_cloudy_day_snow
                hasHail -> if (isNight) R.raw.extreme_night_hail else R.raw.extreme_day_hail
                hasFog -> if (isNight) R.raw.partly_cloudy_night_fog else R.raw.partly_cloudy_day_fog
                hasHaze -> if (isNight) R.raw.partly_cloudy_night_haze else R.raw.partly_cloudy_day_haze
                else -> if (isNight) R.raw.partly_cloudy_night else R.raw.partly_cloudy_day
            }

            // ☁️ CLOUDY/OVERCAST (14, 15, 16, 17)
            14, 15 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast else R.raw.thunderstorms_day_overcast
                hasRain && isExtreme -> if (isNight) R.raw.extreme_night_rain else R.raw.extreme_day_rain
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
                hasRain -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
                hasSnow && isExtreme -> if (isNight) R.raw.extreme_night_snow else R.raw.extreme_day_snow
                hasSnow -> if (isNight) R.raw.overcast_night_snow else R.raw.overcast_day_snow
                hasHail -> if (isNight) R.raw.extreme_night_hail else R.raw.extreme_day_hail
                hasFog -> if (isNight) R.raw.overcast_night_fog else R.raw.overcast_day_fog
                hasHaze -> if (isNight) R.raw.overcast_night_haze else R.raw.overcast_day_haze
                else -> R.raw.cloudy
            }
            16 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast else R.raw.thunderstorms_day_overcast
                hasRain && isExtreme -> if (isNight) R.raw.extreme_night_rain else R.raw.extreme_day_rain
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
                hasRain -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
                hasSnow && isExtreme -> if (isNight) R.raw.extreme_night_snow else R.raw.extreme_day_snow
                hasSnow -> if (isNight) R.raw.overcast_night_snow else R.raw.overcast_day_snow
                else -> if (isNight) R.raw.overcast_night else R.raw.overcast_day
            }
            17 -> when {
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.partly_cloudy_night_drizzle else R.raw.partly_cloudy_day_drizzle
                hasRain -> if (isNight) R.raw.partly_cloudy_night_rain else R.raw.partly_cloudy_day_rain
                hasSnow -> if (isNight) R.raw.partly_cloudy_night_snow else R.raw.partly_cloudy_day_snow
                else -> if (isNight) R.raw.partly_cloudy_night else R.raw.partly_cloudy_day
            }

            // 🌧️ RAIN CONDITIONS (21-26, 31-36, 51-56)
            21, 31 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_rain else R.raw.extreme_day_rain
                else -> if (isNight) R.raw.partly_cloudy_night_rain else R.raw.partly_cloudy_day_rain
            }
            22, 32 -> when {
                isExtreme -> if (isNight) R.raw.thunderstorms_night_overcast_rain else R.raw.thunderstorms_day_overcast_rain
                else -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
            }
            23, 33 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast_rain else R.raw.thunderstorms_day_overcast_rain
                isExtreme -> if (isNight) R.raw.extreme_night_rain else R.raw.extreme_day_rain
                else -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
            }
            24, 34 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_rain else R.raw.thunderstorms_day_rain
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme_rain else R.raw.thunderstorms_day_extreme_rain
                else -> R.raw.rain
            }
            25, 35 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast_rain else R.raw.thunderstorms_day_overcast_rain
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme_rain else R.raw.thunderstorms_day_extreme_rain
                else -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
            }
            26, 36 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_extreme_rain else R.raw.thunderstorms_day_extreme_rain
                else -> R.raw.rain
            }

            // 🌨️ SNOW CONDITIONS (41-46, 61-66)
            41, 61 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_snow else R.raw.extreme_day_snow
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.partly_cloudy_night_drizzle else R.raw.partly_cloudy_day_drizzle
                hasRain -> if (isNight) R.raw.partly_cloudy_night_rain else R.raw.partly_cloudy_day_rain
                else -> if (isNight) R.raw.partly_cloudy_night_snow else R.raw.partly_cloudy_day_snow
            }
            42, 62 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_snow else R.raw.extreme_day_snow
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
                hasRain -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
                else -> if (isNight) R.raw.overcast_night_snow else R.raw.overcast_day_snow
            }
            43, 63 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast_snow else R.raw.thunderstorms_day_overcast_snow
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme_snow else R.raw.thunderstorms_day_extreme_snow
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
                hasRain -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
                else -> if (isNight) R.raw.overcast_night_snow else R.raw.overcast_day_snow
            }
            44, 64 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_snow else R.raw.thunderstorms_day_snow
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme_snow else R.raw.thunderstorms_day_extreme_snow
                descLower.contains("lluvia escasa") -> R.raw.drizzle
                hasRain -> R.raw.rain
                else -> R.raw.snow
            }
            45, 65 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_overcast_snow else R.raw.thunderstorms_day_overcast_snow
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme_snow else R.raw.thunderstorms_day_extreme_snow
                descLower.contains("lluvia escasa") -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
                hasRain -> if (isNight) R.raw.overcast_night_rain else R.raw.overcast_day_rain
                else -> if (isNight) R.raw.overcast_night_snow else R.raw.overcast_day_snow
            }
            46, 66 -> when {
                hasThunder -> if (isNight) R.raw.thunderstorms_night_extreme_snow else R.raw.thunderstorms_day_extreme_snow
                descLower.contains("lluvia escasa") -> R.raw.drizzle
                hasRain -> R.raw.rain
                else -> R.raw.snow
            }

            // 💧 DRIZZLE/LIGHT RAIN (51-56)
            51, 52 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_drizzle else R.raw.extreme_day_drizzle
                else -> if (isNight) R.raw.partly_cloudy_night_drizzle else R.raw.partly_cloudy_day_drizzle
            }
            53, 54 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_drizzle else R.raw.extreme_day_drizzle
                else -> if (isNight) R.raw.overcast_night_drizzle else R.raw.overcast_day_drizzle
            }
            55, 56 -> when {
                isExtreme -> if (isNight) R.raw.extreme_drizzle else R.raw.drizzle
                else -> R.raw.drizzle
            }

            // 🧊 SLEET (71-73)
            71, 72 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_sleet else R.raw.extreme_day_sleet
                else -> if (isNight) R.raw.overcast_night_sleet else R.raw.overcast_day_sleet
            }
            73 -> when {
                isExtreme -> R.raw.extreme_sleet
                else -> R.raw.sleet
            }

            // 🌫️ FOG/MIST (81-83, 86)
            81 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_haze else R.raw.extreme_day_haze
                else -> if (isNight) R.raw.haze_night else R.raw.haze_day
            }
            82, 86 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_fog else R.raw.extreme_day_fog
                else -> if (isNight) R.raw.fog_night else R.raw.fog_day
            }
            83 -> when {
                isExtreme -> if (isNight) R.raw.extreme_fog else R.raw.fog
                else -> R.raw.fog
            }

            // ⛈️ THUNDERSTORMS (91-99)
            91 -> when {
                hasRain -> if (isNight) R.raw.thunderstorms_night_rain else R.raw.thunderstorms_day_rain
                hasSnow -> if (isNight) R.raw.thunderstorms_night_snow else R.raw.thunderstorms_day_snow
                else -> if (isNight) R.raw.thunderstorms_night else R.raw.thunderstorms_day
            }
            92, 95 -> when {
                isExtreme -> if (isNight) R.raw.thunderstorms_night_extreme else R.raw.thunderstorms_night
                else -> if (isNight) R.raw.thunderstorms_night_overcast else R.raw.thunderstorms_day_overcast
            }
            93, 96 -> when {
                hasSnow -> if (isNight) R.raw.thunderstorms_night_extreme_snow else R.raw.thunderstorms_day_extreme_snow
                else -> if (isNight) R.raw.thunderstorms_night_extreme_rain else R.raw.thunderstorms_day_extreme_rain
            }
            94 -> when {
                hasSnow -> if (isNight) R.raw.thunderstorms_night_snow else R.raw.thunderstorms_day_snow
                else -> if (isNight) R.raw.thunderstorms_night_rain else R.raw.thunderstorms_day_rain
            }
            99 -> when {
                isExtreme -> if (isNight) R.raw.extreme_night_hail else R.raw.extreme_night_hail
                else -> if (isNight) R.raw.extreme_night_hail else R.raw.extreme_day_hail
            }

            // 🌪️ EXTREME WEATHER
            100 -> if (isNight) R.raw.extreme_night else R.raw.extreme_day

            else -> R.raw.not_available
        }
    }

    /**
     * Maps UV Index to your specific UV icons (uv_index_1 to uv_index_11)
     */
    @RawRes
    fun getUvIcon(uvIndex: Int?): Int {
        if (uvIndex == null) return R.raw.uv_index
        return when (uvIndex) {
            0, 1 -> R.raw.uv_index_1
            2 -> R.raw.uv_index_2
            3 -> R.raw.uv_index_3
            4 -> R.raw.uv_index_4
            5 -> R.raw.uv_index_5
            6 -> R.raw.uv_index_6
            7 -> R.raw.uv_index_7
            8 -> R.raw.uv_index_8
            9 -> R.raw.uv_index_9
            10 -> R.raw.uv_index_10
            else -> R.raw.uv_index_11 // 11+
        }
    }

    /**
     * Maps Wind Speed (km/h) to the Beaufort scale icons (wind_beaufort_0 to 12)
     */
    @RawRes
    fun getWindIcon(windSpeedKmH: Int?): Int {
        if (windSpeedKmH == null || windSpeedKmH <= 0) return R.raw.wind_beaufort_0
        return when (windSpeedKmH) {
            in 1..5 -> R.raw.wind_beaufort_1
            in 6..11 -> R.raw.wind_beaufort_2
            in 12..19 -> R.raw.wind_beaufort_3
            in 20..28 -> R.raw.wind_beaufort_4
            in 29..38 -> R.raw.wind_beaufort_5
            in 39..49 -> R.raw.wind_beaufort_6
            in 50..61 -> R.raw.wind_beaufort_7
            in 62..74 -> R.raw.wind_beaufort_8
            in 75..88 -> R.raw.wind_beaufort_9
            in 89..102 -> R.raw.wind_beaufort_10
            in 103..117 -> R.raw.wind_beaufort_11
            else -> R.raw.wind_beaufort_12
        }
    }

    /**
     * Maps Wind Direction to specific animated icons
     */
    @RawRes
    fun getWindDirectionIcon(direction: String?): Int {
        return when (direction?.uppercase()) {
            "N" -> R.raw.wind_direction_n
            "S" -> R.raw.wind_direction_s
            "E" -> R.raw.wind_direction_e
            "W", "O" -> R.raw.wind_direction_w
            "NE" -> R.raw.wind_direction_ne
            "NW", "NO" -> R.raw.wind_direction_nw
            "SE" -> R.raw.wind_direction_se
            "SW", "SO" -> R.raw.wind_direction_sw
            else -> R.raw.compass
        }
    }

    /**
     * Returns the animated raindrop icon
     */
    @RawRes
    fun getRaindropIcon(): Int {
        return R.raw.raindrop
    }

    /**
     * Helper function to determine if conditions are extreme based on description
     */
    private fun isExtremeCondition(description: String?): Boolean {
        val desc = description?.lowercase() ?: return false
        return desc.contains("fuerte") ||
                desc.contains("intensa") ||
                desc.contains("persistente") ||
                desc.contains("extreme") ||
                desc.contains("severe")
    }
}