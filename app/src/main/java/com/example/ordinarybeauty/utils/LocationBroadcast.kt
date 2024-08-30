package com.example.ordinarybeauty.utils

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LocationBroadcast {

    var curLocation: Location? = null
    private var subscribers: MutableList<(Location) -> Unit> = mutableListOf()

    fun broadCast(l: Location?){
        if (l !== null){
            curLocation = l
            subscribers.forEach { cb ->
                cb(l)
            }
        }
    }

    fun subscribe(cb: (Location) -> Unit){
        if (!subscribers.contains(cb)){
            subscribers.add(cb)
        }
    }
}