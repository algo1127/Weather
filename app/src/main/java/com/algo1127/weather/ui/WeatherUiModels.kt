package com.algo1127.weather.ui

data class HourlyForecastItem(
    val time: String,
    val temp: Int,
    val aemetCode: String?,
    val description: String?
)

data class DailyForecastItem(
    val dayName: String,
    val skyDescription: String,
    val minTemp: Int,
    val maxTemp: Int,
    val rainProbability: Int,
    val aemetCode: String?
)