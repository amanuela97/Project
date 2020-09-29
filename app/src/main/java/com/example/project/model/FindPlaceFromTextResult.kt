package com.example.project.model

data class FindPlaceFromTextResult(
    val candidates: List<Candidate>,
    val status: String
)

data class Candidate(
    val formatted_address: String,
    val geometry: Geometry,
    val name: String,
    val opening_hours: OpeningHours?,
    val photos: List<Photo>,
    val rating: Double
)

data class PlaceGeometry(
    val location: Location,
    val viewport: Viewport
)

data class PlaceOpeningHours(
    val open_now: Boolean
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
    val northeast: Northeast,
    val southwest: Southwest
)

data class PlaceNortheast(
    val lat: Double,
    val lng: Double
)

data class PlaceSouthwest(
    val lat: Double,
    val lng: Double
)