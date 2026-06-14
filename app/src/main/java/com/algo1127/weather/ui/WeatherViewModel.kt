package com.algo1127.weather.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algo1127.weather.data.repository.WeatherRepository
import com.algo1127.weather.utils.LocationHelper
import com.algo1127.weather.utils.SunriseSunsetCalculator
import kotlinx.coroutines.launch
import java.util.*

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<WeatherUiState>()
    val uiState: LiveData<WeatherUiState> = _uiState

    // Cache of municipalities to avoid re-fetching
    private var allMunicipalities: List<com.algo1127.weather.data.network.AemetMunicipality>? = null

    // 🚨 1-MINUTE API PROTECTION 🚨
    private var lastManualRefreshTime: Long = 0L
    private val REFRESH_COOLDOWN_MS = 1 * 60 * 1000 // 1 minute in milliseconds

    private var isInitialLoad = true

    fun loadWeatherWithLocation(lat: Double, lon: Double, isManualRefresh: Boolean = false) {
        if (isManualRefresh && !isInitialLoad) {
            val timeSinceLastRefresh = System.currentTimeMillis() - lastManualRefreshTime
            if (timeSinceLastRefresh < REFRESH_COOLDOWN_MS) {
                val minutesLeft = (REFRESH_COOLDOWN_MS - timeSinceLastRefresh) / 60000
                val secondsLeft = ((REFRESH_COOLDOWN_MS - timeSinceLastRefresh) % 60000) / 1000
                _cooldownMessage.value = "Data actualizada. Espera ${minutesLeft}m ${secondsLeft}s para refrescar."
                return
            }
        }

        // Show loading if we don't have success state yet
        if (_uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        viewModelScope.launch {
            try {
                // 1. Get all municipalities if not already cached
                if (allMunicipalities == null) {
                    val list = repository.getAllMunicipalities()
                    if (list.isNotEmpty()) {
                        allMunicipalities = list
                    } else {
                        // If we can't get the list, we might have to fallback
                        android.util.Log.e("WeatherVM", "Could not fetch municipalities list")
                    }
                }

                // 2. Find closest municipality based on EXACT GPS coordinates
                val closest = LocationHelper.findClosestMunicipality(lat, lon, allMunicipalities ?: emptyList())
                
                // IMPORTANT: AEMET IDs in the JSON are "id28079", but the API wants "28079".
                // We strip the "id" prefix if it exists.
                val municipioCode = closest?.id?.replace("id", "") ?: "28079" 
                
                android.util.Log.d("WeatherVM", "GPS: ($lat, $lon) -> Closest: ${closest?.nombre} ($municipioCode)")

                // 3. Fetch weather for the identified municipality
                val result = repository.getWeather(municipioCode)
                result.onSuccess { data ->
                    // 4. Calculate Sunrise and Sunset for the EXACT GPS location (more precise than city center)
                    val sunTimes = SunriseSunsetCalculator.calculate(lat, lon, Calendar.getInstance())

                    _uiState.value = WeatherUiState.Success(
                        bundle = data,
                        calculatedSunrise = sunTimes?.first,
                        calculatedSunset = sunTimes?.second
                    )
                    lastManualRefreshTime = System.currentTimeMillis()
                    isInitialLoad = false
                }.onFailure { e ->
                    if (_uiState.value !is WeatherUiState.Success) {
                        _uiState.value = WeatherUiState.Error("Error al cargar clima: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error("Error de ubicación: ${e.message}")
                }
            }
        }
    }

    fun loadWeather(municipioCode: String, isManualRefresh: Boolean = false) {
        // Keep this for backward compatibility or direct calls
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