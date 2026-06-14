package com.algo1127.weather.data.repository

import android.content.Context
import com.algo1127.weather.data.local.WeatherDao
import com.algo1127.weather.data.local.WeatherEntity
import com.algo1127.weather.data.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType

class WeatherRepository(
    private val apiService: AemetApiService,
    private val weatherDao: WeatherDao,
    private val context: Context
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

    private val muniListType: ParameterizedType = Types.newParameterizedType(
        List::class.java,
        AemetMunicipality::class.java
    )
    private val muniAdapter = moshi.adapter<List<AemetMunicipality>>(muniListType)

    private val cacheExpirationTime = 60 * 60 * 1000 // 🆕 Kotlin naming convention (camelCase)

    private var cachedMunicipalities: List<AemetMunicipality>? = null

    suspend fun getAllMunicipalities(): List<AemetMunicipality> {
        // 1. Return memory cache if available
        cachedMunicipalities?.let { return it }

        // 2. Try to load from local assets (Bundled list to avoid SSL BAD_DECRYPT errors)
        try {
            android.util.Log.d("WeatherRepo", "Loading municipalities from assets...")
            val jsonString = context.assets.open("municipios.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val list = muniAdapter.fromJson(jsonString) ?: emptyList()
            
            if (list.isNotEmpty()) {
                android.util.Log.d("WeatherRepo", "Successfully loaded ${list.size} municipalities from assets")
                cachedMunicipalities = list
                return list
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherRepo", "Error loading municipalities from assets", e)
        }

        // 3. Last resort: Try fetching from network (only if assets fail)
        return try {
            android.util.Log.d("WeatherRepo", "Assets failed, fetching municipalities from network...")
            val response = apiService.getAllMunicipalities()
            val list = apiService.getActualMunicipalities(response.datos)
            cachedMunicipalities = list
            list
        } catch (e: Exception) {
            android.util.Log.e("WeatherRepo", "Critical error fetching municipalities", e)
            emptyList()
        }
    }

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

            // DEBUG LOGGING
            freshDaily.firstOrNull()?.prediccion?.dias?.firstOrNull()?.let { today ->
                android.util.Log.d("WeatherRepo", "🌅 Raw Today Data: $today")
                android.util.Log.d("WeatherRepo", "🌅 Sunrise (orto): ${today.orto}, Sunset (ocaso): ${today.ocaso}")
            }

            // 2. Fetch Hourly (with simple retry)
            var freshHourly: List<AemetHourlyResponse>? = null
            var hourlyAttempts = 0
            while (freshHourly == null && hourlyAttempts < 2) {
                try {
                    hourlyAttempts++
                    android.util.Log.d("WeatherRepo", "Fetching hourly data (Attempt $hourlyAttempts) for: $municipioCode")
                    val hourlyInit = apiService.getHourlyInitialData(municipioCode)
                    freshHourly = apiService.getActualHourlyData(hourlyInit.datos)
                    
                    freshHourly.forEach { response ->
                        response.prediccion.dias.forEach { day ->
                            android.util.Log.d("WeatherRepo", "Hourly data for ${day.fecha}: ${day.temperatura?.size ?: 0} temperature points")
                        }
                    }

                    android.util.Log.d("WeatherRepo", "Successfully fetched ${freshHourly.size} hourly response objects")
                } catch (e: Exception) {
                    android.util.Log.e("WeatherRepo", "Hourly fetch attempt $hourlyAttempts failed", e)
                    if (hourlyAttempts < 2) {
                        kotlinx.coroutines.delay(1000) // Small wait before retry
                    }
                }
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