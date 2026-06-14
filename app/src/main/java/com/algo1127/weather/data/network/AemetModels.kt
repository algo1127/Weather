package com.algo1127.weather.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// === TOP-LEVEL RESPONSE ===
@JsonClass(generateAdapter = true)
data class AemetResponse(
    @Json(name = "origen") val origen: Origen,
    @Json(name = "elaborado") val elaborado: String,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "provincia") val provincia: String,
    @Json(name = "prediccion") val prediccion: Prediccion,
    @Json(name = "id") val id: String,
    @Json(name = "version") val version: String
)

// === HOURLY FORECAST DATA ===
@JsonClass(generateAdapter = true)
data class AemetHourlyResponse(
    @Json(name = "origen") val origen: Origen,
    @Json(name = "elaborado") val elaborado: String,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "provincia") val provincia: String,
    @Json(name = "prediccion") val prediccion: PrediccionHourly,
    @Json(name = "id") val id: String,
    @Json(name = "version") val version: String
)

@JsonClass(generateAdapter = true)
data class PrediccionHourly(
    @Json(name = "dia") val dias: List<AemetHourlyDay>
)

@JsonClass(generateAdapter = true)
data class AemetHourlyDay(
    @Json(name = "estadoCielo") val estadoCielo: List<SkyState>?,
    @Json(name = "precipitacion") val precipitacion: List<HourlyValue>?,
    @Json(name = "probPrecipitacion") val probPrecipitacion: List<HourlyValue>?,
    @Json(name = "temperatura") val temperatura: List<HourlyValue>?,
    @Json(name = "sensTermica") val sensTermica: List<HourlyValue>?,
    @Json(name = "humedadRelativa") val humedadRelativa: List<HourlyValue>?,
    @Json(name = "viento") val viento: List<WindHourly>?,
    @Json(name = "fecha") val fecha: String
)

@JsonClass(generateAdapter = true)
data class HourlyValue(
    @Json(name = "value") val value: String?,
    @Json(name = "periodo") val periodo: String?
)

@JsonClass(generateAdapter = true)
data class WindHourly(
    @Json(name = "direccion") val direccion: List<String>?,
    @Json(name = "velocidad") val velocidad: List<String>?,
    @Json(name = "periodo") val periodo: String?
)

// === MUNICIPALITY DATA ===
@JsonClass(generateAdapter = true)
data class AemetMunicipality(
    @Json(name = "id") val id: String?,
    @Json(name = "nombre") val nombre: String?,
    @Json(name = "latitud") val latitud: String?,
    @Json(name = "longitud") val longitud: String?,
    @Json(name = "latitud_dec") val latitudDec: String?,
    @Json(name = "longitud_dec") val longitudDec: String?,
    @Json(name = "altitud") val altitud: String?,
    @Json(name = "provincia") val provincia: String?
)

@JsonClass(generateAdapter = true)
data class Origen(
    @Json(name = "productor") val productor: String,
    @Json(name = "web") val web: String,
    @Json(name = "enlace") val enlace: String,
    @Json(name = "language") val language: String,
    @Json(name = "copyright") val copyright: String,
    @Json(name = "notaLegal") val notaLegal: String
)

@JsonClass(generateAdapter = true)
data class Prediccion(
    @Json(name = "dia") val dias: List<AemetDay>
)

// === DAILY FORECAST DATA ===
@JsonClass(generateAdapter = true)
data class AemetDay(
    @Json(name = "probPrecipitacion") val probPrecipitacion: List<Precipitation>?,
    @Json(name = "cotaNieveProv") val cotaNieveProv: List<CotaNieve>?,
    @Json(name = "estadoCielo") val estadoCielo: List<SkyState>?,
    @Json(name = "viento") val viento: List<Wind>?,
    @Json(name = "rachaMax") val rachaMax: List<RachaMax>?,
    @Json(name = "temperatura") val temperatura: Temperature?,
    @Json(name = "sensTermica") val sensTermica: Temperature?,
    @Json(name = "humedadRelativa") val humedadRelativa: Humidity?,
    @Json(name = "uvMax") val uvMax: Int?,
    @Json(name = "fecha") val fecha: String?,
    @Json(name = "orto") val orto: String?,
    @Json(name = "ocaso") val ocaso: String?
)

// === NESTED DATA STRUCTURES ===
@JsonClass(generateAdapter = true)
data class Precipitation(
    @Json(name = "value") val value: Int?,
    @Json(name = "periodo") val periodo: String?
)

@JsonClass(generateAdapter = true)
data class CotaNieve(
    @Json(name = "value") val value: String?,
    @Json(name = "periodo") val periodo: String?
)

@JsonClass(generateAdapter = true)
data class SkyState(
    @Json(name = "value") val value: String?,
    @Json(name = "periodo") val periodo: String?,
    @Json(name = "descripcion") val descripcion: String?
)

@JsonClass(generateAdapter = true)
data class Wind(
    @Json(name = "direccion") val direccion: String?,
    @Json(name = "velocidad") val velocidad: Int?,
    @Json(name = "periodo") val periodo: String?
)

@JsonClass(generateAdapter = true)
data class RachaMax(
    @Json(name = "value") val value: String?,
    @Json(name = "periodo") val periodo: String?
)

@JsonClass(generateAdapter = true)
data class Temperature(
    @Json(name = "maxima") val maxima: Double?,
    @Json(name = "minima") val minima: Double?,
    @Json(name = "dato") val dato: List<HourlyData>?
)

@JsonClass(generateAdapter = true)
data class HourlyData(
    @Json(name = "value") val value: Double?,
    @Json(name = "hora") val hora: Int?
)

@JsonClass(generateAdapter = true)
data class Humidity(
    @Json(name = "maxima") val maxima: Int?,
    @Json(name = "minima") val minima: Int?,
    @Json(name = "dato") val dato: List<HourlyData>?
)