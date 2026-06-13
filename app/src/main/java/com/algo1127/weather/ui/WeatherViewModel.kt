package com.algo1127.weather.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algo1127.weather.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<WeatherUiState>()
    val uiState: LiveData<WeatherUiState> = _uiState

    // 🚨 5-MINUTE API PROTECTION 🚨
    private var lastManualRefreshTime: Long = 0L
    private val REFRESH_COOLDOWN_MS = 5 * 60 * 1000 // 5 minutes in milliseconds

    private var isInitialLoad = true

    fun loadWeather(municipioCode: String, isManualRefresh: Boolean = false) {
        // 1. Enforce the 5-minute cooldown for manual refreshes
        if (isManualRefresh && !isInitialLoad) {
            val timeSinceLastRefresh = System.currentTimeMillis() - lastManualRefreshTime
            if (timeSinceLastRefresh < REFRESH_COOLDOWN_MS) {
                val minutesLeft = (REFRESH_COOLDOWN_MS - timeSinceLastRefresh) / 60000
                val secondsLeft = ((REFRESH_COOLDOWN_MS - timeSinceLastRefresh) % 60000) / 1000

                // Don't change UI state to Error - just notify via a callback/event
                // The ViewModel can't show Toasts directly, so we'll use a separate LiveData
                _cooldownMessage.value = "Data actualizada. Espera ${minutesLeft}m ${secondsLeft}s para refrescar."
                return
            }
        }

        // 2. Tell UI to show loading spinner ONLY if we don't have cached data
        if (_uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        // 3. Fetch data in the background
        viewModelScope.launch {
            val result = repository.getWeather(municipioCode)
            result.onSuccess { data ->
                _uiState.value = WeatherUiState.Success(data)
                lastManualRefreshTime = System.currentTimeMillis()
                isInitialLoad = false
            }.onFailure { e ->
                // Only show error if we don't have existing data
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error(e.message ?: "Error desconocido")
                }
                // If we HAVE data, just log the error - don't disturb the user
            }
        }
    }

    // Add this LiveData for transient messages
    private val _cooldownMessage = MutableLiveData<String>()
    val cooldownMessage: LiveData<String> = _cooldownMessage
}