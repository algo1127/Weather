package com.algo1127.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.algo1127.weather.data.local.WeatherDatabase
import com.algo1127.weather.data.network.RetrofitClient
import com.algo1127.weather.data.repository.WeatherRepository
import com.algo1127.weather.ui.AemetIconMapper
import com.algo1127.weather.ui.DailyAdapter
import com.algo1127.weather.ui.DailyForecastItem
import com.algo1127.weather.ui.HourlyAdapter
import com.algo1127.weather.ui.HourlyForecastItem
import com.algo1127.weather.ui.WeatherUiState
import com.algo1127.weather.ui.WeatherViewModel
import com.algo1127.weather.ui.WeatherViewModelFactory
import com.algo1127.weather.utils.LocationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: WeatherViewModel

    // Standard UI Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvCityName: TextView
    private lateinit var tvProvince: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvTempRange: TextView
    private lateinit var tvSkyCondition: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvWindGust: TextView
    private lateinit var tvUV: TextView
    private lateinit var tvRain: TextView
    private lateinit var tvSunrise: TextView
    private lateinit var tvSunset: TextView
    private lateinit var tvLegalFooter: TextView
    private lateinit var btnRefresh: Button
    private lateinit var rvHourlyForecast: RecyclerView
    private lateinit var rvDailyForecast: RecyclerView

    // 🎨 Lottie Animation Views
    private lateinit var lottieHeroIcon: LottieAnimationView
    private lateinit var lottieWindIcon: LottieAnimationView
    private lateinit var lottieUvIcon: LottieAnimationView
    private lateinit var lottieRainIcon: LottieAnimationView
    private lateinit var lottieSunriseIcon: LottieAnimationView
    private lateinit var lottieSunsetIcon: LottieAnimationView

    private lateinit var lottieHumidityIcon: LottieAnimationView // 🆕 AÑADIR ESTA LÍNEA

    private lateinit var skyBackground: SkyBackgroundView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            refreshWeatherWithLocation(isManualRefresh = false)
        } else {
            Toast.makeText(this, "Permiso denegado. Usando ubicación predeterminada (Madrid)", Toast.LENGTH_LONG).show()
            viewModel.loadWeather("28079")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        val apiService = RetrofitClient.apiService
        val weatherDao = WeatherDatabase.getDatabase(this).weatherDao()
        val repository = WeatherRepository(apiService, weatherDao, this) // Added 'this' as context

        val factory = WeatherViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

        viewModel.uiState.observe(this) { state ->
            handleUiState(state)
        }

        viewModel.cooldownMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        btnRefresh.setOnClickListener {
            refreshWeatherWithLocation(isManualRefresh = true)
        }

        rvHourlyForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDailyForecast.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState == null) {
            checkPermissionsAndLoadWeather()
        }
    }

    private fun checkPermissionsAndLoadWeather() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                refreshWeatherWithLocation(isManualRefresh = false)
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun refreshWeatherWithLocation(isManualRefresh: Boolean) {
        lifecycleScope.launch {
            val location = LocationHelper.getCurrentLocation(this@MainActivity)
            if (location != null) {
                android.util.Log.d("WeatherMain", "Location found: ${location.first}, ${location.second}")
                viewModel.loadWeatherWithLocation(location.first, location.second, isManualRefresh)
            } else {
                if (!isManualRefresh) {
                    // Fallback to Madrid only if we absolutely can't get the location on first boot
                    viewModel.loadWeather("28079") 
                } else {
                    Toast.makeText(this@MainActivity, "No se pudo obtener la ubicación GPS actual", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvCityName = findViewById(R.id.tvCityName)
        tvProvince = findViewById(R.id.tvProvince)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvTempRange = findViewById(R.id.tvTempRange)
        tvSkyCondition = findViewById(R.id.tvSkyCondition)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWind = findViewById(R.id.tvWind)
        tvWindGust = findViewById(R.id.tvWindGust)
        tvUV = findViewById(R.id.tvUV)
        tvRain = findViewById(R.id.tvRain)
        tvSunrise = findViewById(R.id.tvSunrise)
        tvSunset = findViewById(R.id.tvSunset)
        tvLegalFooter = findViewById(R.id.tvLegalFooter)
        btnRefresh = findViewById(R.id.btnRefresh)
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast)
        rvDailyForecast = findViewById(R.id.rvDailyForecast)

        // Initialize Lottie Views
        lottieHeroIcon = findViewById(R.id.lottieHeroIcon)
        lottieWindIcon = findViewById(R.id.lottieWindIcon)
        lottieUvIcon = findViewById(R.id.lottieUvIcon)
        lottieRainIcon = findViewById(R.id.lottieRainIcon)
        lottieHumidityIcon = findViewById(R.id.lottieHumidityIcon)
        lottieSunriseIcon = findViewById(R.id.lottieSunriseIcon)
        lottieSunsetIcon = findViewById(R.id.lottieSunsetIcon)

        skyBackground = findViewById(R.id.skyBackground)
    }

    private fun determineWeatherCondition(weatherData: com.algo1127.weather.data.network.AemetResponse?): SkyBackgroundView.WeatherCondition {
        if (weatherData == null) return SkyBackgroundView.WeatherCondition.SUNNY

        val today = weatherData.prediccion.dias.firstOrNull() ?: return SkyBackgroundView.WeatherCondition.SUNNY

        // Check for night (if the current time is between 8pm and 6am)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 20 || hour < 6

        // Check for stormy conditions
        val uvMax = today.uvMax ?: 0
        val rainProb = today.probPrecipitacion?.firstOrNull { it.periodo == "00-24" }?.value ?: 0

        if (isNight) {
            if (rainProb > 50) return SkyBackgroundView.WeatherCondition.STORMY
            if (rainProb > 20) return SkyBackgroundView.WeatherCondition.RAINY
            return SkyBackgroundView.WeatherCondition.NIGHT
        }

        // Check for stormy conditions
        if (rainProb > 70 && uvMax < 3) return SkyBackgroundView.WeatherCondition.STORMY
        if (rainProb > 40) return SkyBackgroundView.WeatherCondition.RAINY
        if (rainProb > 20) return SkyBackgroundView.WeatherCondition.CLOUDY

        // Check for snow
        val snowLevelStr = today.cotaNieveProv?.firstOrNull()?.value
        val snowLevel = snowLevelStr?.toDoubleOrNull() ?: 0.0
        if (snowLevel > 0 && rainProb > 30) return SkyBackgroundView.WeatherCondition.SNOWY

        // Check for fog
        val skyDesc = today.estadoCielo?.firstOrNull { !it.descripcion.isNullOrBlank() }?.descripcion ?: ""
        if (skyDesc.contains("niebla", ignoreCase = true) ||
            skyDesc.contains("calima", ignoreCase = true)) {
            return SkyBackgroundView.WeatherCondition.FOGGY
        }

        // Default to sunny
        return SkyBackgroundView.WeatherCondition.SUNNY
    }

    private fun handleUiState(state: WeatherUiState) {
        when (state) {
            is WeatherUiState.Loading -> {
                progressBar.visibility = View.VISIBLE
                tvError.visibility = View.GONE
            }
            is WeatherUiState.Success -> {
                progressBar.visibility = View.GONE
                tvError.visibility = View.GONE



                val weatherData = state.bundle.daily.firstOrNull() ?: return
                val today = weatherData.prediccion.dias.firstOrNull() ?: return

                // 1. Header
                tvCityName.text = weatherData.nombre
                tvProvince.text = weatherData.provincia

                // 2. Hero Section
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val currentHourStr = String.format("%02d", currentHour)
                val hourlyDays = state.bundle.hourly?.firstOrNull()?.prediccion?.dias ?: emptyList()
                val todayHourly = hourlyDays.firstOrNull()

                val currentTemp = todayHourly?.temperatura?.find { it.periodo == currentHourStr }?.value?.toIntOrNull()
                    ?: today.temperatura?.maxima?.toInt()

                val maxTemp = today.temperatura?.maxima?.toInt()
                val minTemp = today.temperatura?.minima?.toInt()
                tvTemperature.text = if (currentTemp != null) "${currentTemp}°" else "N/A"
                tvTempRange.text = "Min ${minTemp ?: "N/A"}°  •  Max ${maxTemp ?: "N/A"}°"

                val skyState = today.estadoCielo?.firstOrNull { !it.descripcion.isNullOrBlank() }
                tvSkyCondition.text = skyState?.descripcion ?: "N/A"

                val currentFeelsLike = todayHourly?.sensTermica?.find { it.periodo == currentHourStr }?.value?.toIntOrNull()
                    ?: today.sensTermica?.maxima?.toInt()
                tvFeelsLike.text = if (currentFeelsLike != null) "Sensación térmica: ${currentFeelsLike}°" else ""

                // 🎨 HERO ICON
                lottieHeroIcon.setAnimation(AemetIconMapper.getWeatherIcon(skyState?.value, skyState?.descripcion))
                lottieHeroIcon.playAnimation()

                // 3. Detailed Stats
                val minHum = today.humedadRelativa?.minima
                val maxHum = today.humedadRelativa?.maxima
                tvHumidity.text = if (minHum != null && maxHum != null) "${minHum}% - ${maxHum}%" else "N/A"

                // 🆕 AÑADIR ESTAS 2 LÍNEAS PARA EL ICONO DE HUMEDAD
                lottieHumidityIcon.setAnimation(R.raw.humidity)
                lottieHumidityIcon.playAnimation()

                var maxSpeed = 0
                val windData = today.viento?.filter {
                    !it.direccion.isNullOrBlank() && it.velocidad != null && it.velocidad > 0
                }
                if (!windData.isNullOrEmpty()) {
                    val maxWind = windData.maxByOrNull { it.velocidad ?: 0 }
                    val minWind = windData.minByOrNull { it.velocidad ?: 0 }
                    maxSpeed = maxWind?.velocidad ?: 0
                    val minSpeed = minWind?.velocidad ?: 0
                    val direction = maxWind?.direccion ?: ""
                    tvWind.text = "${minSpeed} km/h"

                    val maxGust = today.rachaMax?.maxOfOrNull { it.value?.toIntOrNull() ?: 0 }
                    tvWindGust.text = if (maxGust != null && maxGust > 0) "Rachas: $maxGust km/h" else ""

                    // 🎨 WIND ICON (Now using direction animated icons)
                    lottieWindIcon.setAnimation(AemetIconMapper.getWindDirectionIcon(direction))
                } else {
                    tvWind.text = "Calma"
                    tvWindGust.text = ""
                    lottieWindIcon.setAnimation(R.raw.wind_beaufort_0)
                }

                lottieWindIcon.playAnimation()

                val uvMax = today.uvMax
                val uvDescription = when {
                    uvMax == null -> "N/A"
                    uvMax <= 2 -> "Bajo"
                    uvMax <= 5 -> "Moderado"
                    uvMax <= 7 -> "Alto"
                    uvMax <= 10 -> "Muy Alto"
                    else -> "Extremo"
                }
                tvUV.text = if (uvMax != null) "$uvMax ($uvDescription)" else "N/A"

                // 🎨 UV ICON
                lottieUvIcon.setAnimation(AemetIconMapper.getUvIcon(uvMax))
                lottieUvIcon.playAnimation()

                val rainProb = today.probPrecipitacion?.firstOrNull { it.periodo == "00-24" }?.value ?: 0
                tvRain.text = "${rainProb}%"

                // 🎨 RAIN ICON (Mapped by probability)
                val rainIconRes = when {
                    rainProb > 70 -> R.raw.rain
                    rainProb > 30 -> R.raw.raindrops
                    rainProb > 0 -> R.raw.partly_cloudy_day_rain
                    else -> R.raw.cloudy
                }
                lottieRainIcon.setAnimation(rainIconRes)
                lottieRainIcon.playAnimation()

                // 🌅 SUNRISE & SUNSET (Calculated if available, otherwise fallback to API)
                tvSunrise.text = state.calculatedSunrise ?: today.orto ?: "N/A"
                lottieSunriseIcon.setAnimation(R.raw.sunrise)
                lottieSunriseIcon.playAnimation()

                tvSunset.text = state.calculatedSunset ?: today.ocaso ?: "N/A"
                lottieSunsetIcon.setAnimation(R.raw.sunset)
                lottieSunsetIcon.playAnimation()

                // 4. Legal Footer
                val elaborado = weatherData.elaborado
                val formattedDate = if (elaborado != null) {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        val date = inputFormat.parse(elaborado)
                        outputFormat.format(date ?: Date())
                    } catch (e: Exception) {
                        elaborado.take(16).replace("T", " ")
                    }
                } else {
                    "Reciente"
                }
                tvLegalFooter.text = "Actualizado: $formattedDate | © AEMET"

                // NEW: Update the sky background
                val weatherCondition = determineWeatherCondition(weatherData)
                skyBackground.setWeatherCondition(weatherCondition)

                // 5. Populate RecyclerViews
                // --- HOURLY FORECAST ---
                val hourlyItems = mutableListOf<HourlyForecastItem>()

                // USE THE REAL HOURLY DATA (48h API) - Already defined as 'hourlyDays' above
                val currentHourVal = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                hourlyDays.forEachIndexed { dayIndex, day ->
                    day.temperatura?.forEachIndexed { hourIndex, tempVal ->
                        val timeStr = tempVal.periodo ?: ""
                        val hour = timeStr.toIntOrNull() ?: -1

                        // For the first day, only show current and future hours. 
                        // For subsequent days, show all hours.
                        if (dayIndex > 0 || hour >= currentHourVal) {
                            val temp = tempVal.value?.toIntOrNull() ?: 0
                            val sky = day.estadoCielo?.getOrNull(hourIndex)

                            hourlyItems.add(
                                HourlyForecastItem(
                                    "${timeStr}:00",
                                    temp,
                                    sky?.value,
                                    sky?.descripcion
                                )
                            )
                        }
                    }
                }

                if (hourlyItems.isNotEmpty()) {
                    android.util.Log.d("WeatherMain", "Populating ${hourlyItems.size} hourly items from 48h API")
                } else {
                    android.util.Log.w("WeatherMain", "Hourly list from 48h API is empty, falling back to 6h chunks")
                    today.temperatura?.dato?.forEach { tempData ->
                        val hour = tempData.hora ?: 0
                        if (hour >= currentHourVal) {
                            val timeString = String.format("%02d:00", hour)
                            val temp = tempData.value?.toInt() ?: 0
                            hourlyItems.add(
                                HourlyForecastItem(
                                    timeString,
                                    temp,
                                    skyState?.value,
                                    skyState?.descripcion
                                )
                            )
                        }
                    }
                }

                rvHourlyForecast.adapter = HourlyAdapter(hourlyItems)

                // --- 7-DAY FORECAST ---
                val dailyItems = mutableListOf<DailyForecastItem>()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale("es", "ES"))
                val shortDateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale("es", "ES"))

                weatherData.prediccion.dias.forEachIndexed { index, day ->
                    val date = try {
                        day.fecha?.let { dateFormat.parse(it) }
                    } catch (e: Exception) {
                        null
                    }

                    val dayName = when (index) {
                        0 -> "Hoy"
                        1 -> "Mañana"
                        else -> date?.let { dayFormat.format(it).replaceFirstChar { c -> c.uppercase() } } ?: "Día ${index + 1}"
                    }
                    val dateStr = date?.let { shortDateFormat.format(it) } ?: ""

                    val min = day.temperatura?.minima?.toInt() ?: 0
                    val max = day.temperatura?.maxima?.toInt() ?: 0

                    val daySkyDesc = day.estadoCielo?.firstOrNull { !it.descripcion.isNullOrBlank() }?.descripcion ?: "N/A"
                    val daySkyCode = day.estadoCielo?.firstOrNull { !it.value.isNullOrBlank() }?.value

                    val dayRainProb = day.probPrecipitacion?.firstOrNull { it.periodo == "00-24" }?.value
                        ?: day.probPrecipitacion?.maxOfOrNull { it.value ?: 0 }
                        ?: 0

                    dailyItems.add(
                        DailyForecastItem(
                            "$dayName\n$dateStr",
                            daySkyDesc,
                            min,
                            max,
                            dayRainProb,
                            daySkyCode // Pass the code to the adapter!
                        )
                    )
                }
                android.util.Log.d("WeatherTest", "📅 Total days built for adapter: ${dailyItems.size}")
                rvDailyForecast.adapter = DailyAdapter(dailyItems)
            }
            is WeatherUiState.Error -> {
                progressBar.visibility = View.GONE
                tvError.visibility = View.VISIBLE
                tvError.text = "⚠️ ${state.message}"
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}