package com.algo1127.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    // Fetch the cached weather for a specific city
    @Query("SELECT * FROM weather_cache WHERE municipioCode = :code LIMIT 1")
    suspend fun getWeatherByCode(code: String): WeatherEntity?

    // Save new weather data. If it already exists, REPLACE it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)
}