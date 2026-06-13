package com.algo1127.weather.ui

import com.algo1127.weather.data.network.AemetResponse
import com.algo1127.weather.data.repository.WeatherBundle

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    // Por esto:
    data class Success(val bundle: WeatherBundle) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}