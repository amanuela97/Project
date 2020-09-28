package com.example.project.room_data

import android.graphics.Bitmap
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val icon: String,
    val business_status: String,
    val photos: Byte,
    val place_id: String,
    val rating: Double,
    val reference: String,
    val vicinity: String,
    @Embedded
    val location: Location,
)

data class Location(
    val lat: Double,
    val lng: Double
)