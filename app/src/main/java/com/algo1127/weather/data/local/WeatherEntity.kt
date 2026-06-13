package com.algo1127.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val municipioCode: String,
    val municipioName: String,
    val provincia: String,
    val lastUpdated: Long,
    val dailyForecastJson: String,
    val hourlyForecastJson: String? = null // 🆕 NUEVO para las 48 horas
)