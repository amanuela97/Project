package com.example.project.api


import com.example.project.model.NearbySearch
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApi {

    @GET("place/nearbysearch/json")
    suspend fun getNearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: String,
        @Query("type") types: String,
        @Query("key") key: String
    ): NearbySearch

}