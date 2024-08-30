package com.example.ordinarybeauty.data

import java.util.Date

data class PhotoSpot (
    val id: Int = 0, //redni broj slike vlasnika
    val key: String = "", //uid vlasnika
    val title: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val type: Type = Type.Unspecified,
    val datePosted: Date = Date(),
    val authorName: String = ""
){
    var rating: Float = 0F
    var comments: List<String> = listOf()
    var dateLastInteraction: Date = Date()
    var rates: List<String> = listOf()
}

enum class Type {
    Buildings,
    Nature,
    Unspecified
}