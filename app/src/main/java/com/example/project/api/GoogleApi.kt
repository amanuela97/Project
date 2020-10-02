package com.example.project.api


import com.example.project.model.FindPlaceFromTextResult
import com.example.project.model.NearbySearch
import com.example.project.model.PlaceDetails
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

    @GET("place/findplacefromtext/json")
    suspend fun findPlaceFromText(
        @Query("input") input: String,
        @Query("inputtype") inputtype: String,
        @Query("fields") fields: String,
        @Query("key")  key: String
    ): FindPlaceFromTextResult

    @GET("place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") place_id: String,
        @Query("fields") fields: String,
        @Query("key")  key: String
    ): PlaceDetails

    @GET("place/details/json")
    suspend fun getPlaceDetails2(
        @Query("place_id") place_id: String,
        @Query("fields") fields: String,
        @Query("key")  key: String
    ): PlaceDetails


}