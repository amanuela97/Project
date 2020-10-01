package com.example.project.model


data class FindPlaceFromTextResult(
    val candidates: List<Candidate>,
    val status: String
)

data class Candidate(
    val place_id: String
)