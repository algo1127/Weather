package com.algo1127.weather.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // TODO: PASTE YOUR AEMET API KEY HERE
    private const val API_KEY = "APIKEY"
    private const val BASE_URL = "https://opendata.aemet.es/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            // FIX 1: Add User-Agent and Accept headers.
            // This stops AEMET's firewall from dropping "scraper-looking" connections.
            .header("User-Agent", "PotatoWeather/1.0 (Android)")
            .header("Accept", "application/json")

        // FIX 2: Only add the API key to the initial /api/ call.
        if (original.url.encodedPath.contains("/api/")) {
            requestBuilder.header("api_key", API_KEY)
        }

        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .retryOnConnectionFailure(true) // FIX 3: Automatically retry if AEMET drops the connection
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: AemetApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AemetApiService::class.java)
    }
}