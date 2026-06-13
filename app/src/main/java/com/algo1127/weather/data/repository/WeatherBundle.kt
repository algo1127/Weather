package com.algo1127.weather.data.repository

import com.algo1127.weather.data.network.AemetHourlyResponse
import com.algo1127.weather.data.network.AemetResponse

data class WeatherBundle(
    val daily: List<AemetResponse>,
    val hourly: List<AemetHourlyResponse>?
)