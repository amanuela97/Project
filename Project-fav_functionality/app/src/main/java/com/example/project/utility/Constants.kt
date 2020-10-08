package com.example.project.utility

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class Constants {
    companion object{
        const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/"
        const val INPUT_TYPE = "textquery"
        const val FIELDS =  "place_id"
        const val FIELDS_2 = "name,rating,formatted_phone_number,opening_hours,formatted_address,geometry,photo"

        const val TAG = "GGM"
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val DEFAULT_ZOOM = 12

        // Keys for storing activity state.
        const val KEY_CAMERA_POSITION = "camera_position"
        const val KEY_LOCATION = "location"

        const val TYPE = "restaurant"
        const val OPEN_WEATHER_URL = "http://api.openweathermap.org/data/"
        const val  OPEN_WEATHER_API = "26c1da7519547a4421ecbe0e165bba36"
        const val UNIT = "metric"
        var Location: LatLng? = null
    }
}