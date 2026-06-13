package com.algo1127.weather.data.repository

import com.algo1127.weather.data.local.WeatherDao
import com.algo1127.weather.data.local.WeatherEntity
import com.algo1127.weather.data.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType

class WeatherRepository(
    private val apiService: AemetApiService,
    private val weatherDao: WeatherDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val dailyListType: ParameterizedType = Types.newParameterizedType(
        List::class.java,
        AemetResponse::class.java
    )
    private val dailyAdapter = moshi.adapter<List<AemetResponse>>(dailyListType)

    private val hourlyListType: ParameterizedType = Types.newParameterizedType(
        List::class.java,
        AemetHourlyResponse::class.java
    )
    private val hourlyAdapter = moshi.adapter<List<AemetHourlyResponse>>(hourlyListType)

    private val cacheExpirationTime = 60 * 60 * 1000 // 🆕 Kotlin naming convention (camelCase)

    suspend fun getWeather(municipioCode: String): Result<WeatherBundle> {
        return try {
            val cachedEntity = weatherDao.getWeatherByCode(municipioCode)
            val currentTime = System.currentTimeMillis()

            if (cachedEntity != null && (currentTime - cachedEntity.lastUpdated) < cacheExpirationTime) {
                val cachedDaily = dailyAdapter.fromJson(cachedEntity.dailyForecastJson) ?: emptyList()
                val cachedHourly = cachedEntity.hourlyForecastJson?.let { hourlyAdapter.fromJson(it) }
                return Result.success(WeatherBundle(cachedDaily, cachedHourly))
            }

            // 1. Fetch Daily
            val dailyInit = apiService.getDailyInitialData(municipioCode)
            val freshDaily = apiService.getActualDailyData(dailyInit.datos) // 🆕 'datos' not 'datosUrl'

            // 2. Fetch Hourly
            var freshHourly: List<AemetHourlyResponse>? = null
            try {
                val hourlyInit = apiService.getHourlyInitialData(municipioCode)
                freshHourly = apiService.getActualHourlyData(hourlyInit.datos) // 🆕 'datos' not 'datosUrl'
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Save to DB
            val newEntity = WeatherEntity(
                municipioCode = municipioCode,
                municipioName = freshDaily.firstOrNull()?.nombre ?: "Unknown",
                provincia = freshDaily.firstOrNull()?.provincia ?: "Unknown",
                lastUpdated = currentTime,
                dailyForecastJson = dailyAdapter.toJson(freshDaily),
                hourlyForecastJson = freshHourly?.let { hourlyAdapter.toJson(it) }
            )
            weatherDao.insertWeather(newEntity)

            Result.success(WeatherBundle(freshDaily, freshHourly))
        } catch (e: Exception) {
            val cachedEntity = weatherDao.getWeatherByCode(municipioCode)
            if (cachedEntity != null) {
                val cachedDaily = dailyAdapter.fromJson(cachedEntity.dailyForecastJson) ?: emptyList()
                val cachedHourly = cachedEntity.hourlyForecastJson?.let { hourlyAdapter.fromJson(it) }
                return Result.success(WeatherBundle(cachedDaily, cachedHourly))
            }
            Result.failure(e)
        }
    }
}