package com.example.project.api


import com.example.project.model.WeatherResult
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?
    ): WeatherResult
}