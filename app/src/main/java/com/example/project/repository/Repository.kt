package com.example.project.repository

import com.example.project.api.RetrofitInstance

class Repository {
    suspend fun getNearbySearch(location: String, radius: String, types: String, key: String) =
        RetrofitInstance.api.getNearbySearch(location, radius, types, key)

    suspend fun findPlaceFromText(input: String, inputtype: String, fields: String, key: String) =
        RetrofitInstance.api.findPlaceFromText(input,inputtype,fields,key)

    suspend fun getPlaceDetails(place_id: String, fields: String, key: String) =
        RetrofitInstance.api.getPlaceDetails(place_id,fields,key)

    suspend fun getPlaceDetails2(place_id: String, fields: String, key: String) =
        RetrofitInstance.api.getPlaceDetails2(place_id,fields,key)
}