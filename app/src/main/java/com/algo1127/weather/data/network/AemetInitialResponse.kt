package com.algo1127.weather.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AemetInitialResponse(
    @Json(name = "datos") val datos: String,
    @Json(name = "estado") val estado: Int,
    @Json(name = "metadatos") val metadatos: String?
)