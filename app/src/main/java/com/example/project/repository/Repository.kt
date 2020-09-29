package com.example.project.repository

import com.example.project.api.RetrofitInstance

class Repository {
    suspend fun getNearbySearch(location: String, radius: String, types: String, key: String) =
        RetrofitInstance.api.getNearbySearch(location, radius, types, key)
}