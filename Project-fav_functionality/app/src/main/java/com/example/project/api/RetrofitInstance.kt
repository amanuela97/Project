package com.example.project.api

import com.example.project.utility.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    private val retrofit2 by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.OPEN_WEATHER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    val api: GoogleApi by lazy {
        retrofit.create(GoogleApi::class.java)
    }

    val api2: WeatherAPI by lazy {
        retrofit2.create(WeatherAPI::class.java)
    }


}