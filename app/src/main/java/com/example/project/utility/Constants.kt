package com.example.project.utility

class Constants {
    companion object{
        const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/"
        const val INPUT_TYPE = "textquery"
        const val FIELDS =  "photos,formatted_address,name,rating,opening_hours,geometry"

        const val TAG = "GGM"
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val DEFAULT_ZOOM = 12

        // Keys for storing activity state.
        const val KEY_CAMERA_POSITION = "camera_position"
        const val KEY_LOCATION = "location"

        const val RADIUS_1000 = "1000"
        const val TYPE = "restaurant"
    }
}