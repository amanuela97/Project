package com.example.project.model

data class PlaceDetails(
    val html_attributions: List<Any>,
    val result: PlaceDetailsResult,
    val status: String
)

data class PlaceDetailsResult(
    val formatted_address: String,
    val formatted_phone_number: String,
    val geometry: PlaceGeometry,
    val name: String,
    val opening_hours: PlaceOpeningHours?,
    val photos: List<PlacePhoto>,
    val rating: Double
)

data class PlaceGeometry(
    val location: PlaceLocation,
    val viewport: PlaceViewport
)

data class PlaceOpeningHours(
    val open_now: Boolean,
    val periods: List<PlacePeriod>,
    val weekday_text: List<String>
)

data class PlacePhoto(
    val height: Int,
    val html_attributions: List<String>,
    val photo_reference: String,
    val width: Int
)

data class PlaceLocation(
    val lat: Double,
    val lng: Double
)

data class PlaceViewport(
    val northeast: PlaceNortheast,
    val southwest: PlaceSouthwest
)

data class PlaceNortheast(
    val lat: Double,
    val lng: Double
)

data class PlaceSouthwest(
    val lat: Double,
    val lng: Double
)

data class PlacePeriod(
    val close: Close,
    val `open`: Open
)

data class Close(
    val day: Int,
    val time: String
)

data class Open(
    val day: Int,
    val time: String
)