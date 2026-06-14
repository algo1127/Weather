package com.algo1127.weather.utils

import java.util.*
import kotlin.math.*

/**
 * A simple implementation of the solar algorithm to calculate sunrise and sunset.
 * Based on the algorithm from the Almanac for Computers (1990).
 */
object SunriseSunsetCalculator {

    fun calculate(lat: Double, lon: Double, date: Calendar): Pair<String, String>? {
        val sunrise = calculateTime(lat, lon, date, true)
        val sunset = calculateTime(lat, lon, date, false)

        if (sunrise == null || sunset == null) return null

        return Pair(formatTime(sunrise, date), formatTime(sunset, date))
    }

    private fun calculateTime(lat: Double, lon: Double, date: Calendar, isSunrise: Boolean): Double? {
        val zenith = 90.83333333333333 // Official zenith
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR)

        // 1. first calculate the estimated time
        val lngHour = lon / 15.0
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - lngHour) / 24.0)
        } else {
            dayOfYear + ((18.0 - lngHour) / 24.0)
        }

        // 2. calculate the Sun's mean anomaly
        val M = (0.9856 * t) - 3.2891

        // 3. calculate the Sun's true longitude
        var L = M + (1.916 * sin(Math.toRadians(M))) + (0.020 * sin(Math.toRadians(2 * M))) + 282.634
        L = normalize(L, 360.0)

        // 4. calculate the Sun's right ascension
        var RA = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(L))))
        RA = normalize(RA, 360.0)

        // 5. right ascension value needs to be in the same quadrant as L
        val lQuadrant = floor(L / 90.0) * 90.0
        val raQuadrant = floor(RA / 90.0) * 90.0
        RA += (lQuadrant - raQuadrant)

        // 6. right ascension value needs to be converted into hours
        RA /= 15.0

        // 7. calculate the Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(L))
        val cosDec = cos(asin(sinDec))

        // 8. calculate the Sun's local hour angle
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))

        if (cosH > 1) return null // Sun never rises
        if (cosH < -1) return null // Sun never sets

        // 9. finish calculating H and convert into hours
        var H = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }
        H /= 15.0

        // 10. calculate local mean time of rising/setting
        val T = H + RA - (0.06571 * t) - 6.622

        // 11. adjust back to UTC
        var UT = T - lngHour
        UT = normalize(UT, 24.0)

        // 12. convert UT value to local time zone of latitude/longitude
        val localOffset = date.timeZone.getOffset(date.timeInMillis) / 3600000.0
        return normalize(UT + localOffset, 24.0)
    }

    private fun normalize(value: Double, max: Double): Double {
        var v = value
        while (v < 0) v += max
        while (v >= max) v -= max
        return v
    }

    private fun formatTime(time: Double, date: Calendar): String {
        val hours = floor(time).toInt()
        val minutes = floor((time - hours) * 60.0).toInt()
        return String.format("%02d:%02d", hours, minutes)
    }
}