package com.example.ordinarybeauty.data

data class User(
    val username: String = "",
    val password: String = "",
    val fullname: String = "",
    val phone: String = ""
){
    var points: Int = 0
    var numPhotos: Int = 0
}