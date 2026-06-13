package com.algo1127.weather.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface AemetApiService {

    // Daily forecast endpoints
    @GET("opendata/api/prediccion/especifica/municipio/diaria/{codigoMunicipio}")
    suspend fun getDailyInitialData(
        @Path("codigoMunicipio") municipio: String
    ): AemetInitialResponse

    @GET
    suspend fun getActualDailyData(
        @Url datosUrl: String
    ): List<AemetResponse>

    // 🆕 Hourly forecast endpoints
    @GET("opendata/api/prediccion/especifica/municipio/horaria/{codigoMunicipio}")
    suspend fun getHourlyInitialData(
        @Path("codigoMunicipio") municipio: String
    ): AemetInitialResponse

    @GET
    suspend fun getActualHourlyData(
        @Url datosUrl: String
    ): List<AemetHourlyResponse>

    // 🆕 Master municipalities list (for GPS)
    @GET("opendata/api/maestro/municipios/")
    suspend fun getAllMunicipalities(): List<AemetMunicipality>
}