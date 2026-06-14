package com.algo1127.weather.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // TODO: PASTE YOUR AEMET API KEY HERE
    private const val API_KEY = "API_HERE"
    private const val BASE_URL = "https://opendata.aemet.es/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            // USE A REAL BROWSER USER-AGENT: This is the most effective way to avoid 
            // AEMET's "BAD_DECRYPT" which is often a stealthy rate-limit or bot-block.
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
            .header("Connection", "keep-alive")

        if (original.url.encodedPath.contains("/api/") && !original.url.encodedPath.contains("/maestro/")) {
            requestBuilder.header("api_key", API_KEY)
        }

        if (original.url.encodedPath.contains("/maestro/municipios")) {
            requestBuilder.header("api_key", API_KEY)
        }

        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        // REMOVED Protocol.HTTP_1_1 restriction to allow HTTP/2 if the server supports it better
        .retryOnConnectionFailure(true)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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