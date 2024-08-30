package com.example.ordinarybeauty.viewmodels

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.Type
import java.util.Date

class PhotoSpotViewModel : ViewModel() {
    var title: String by mutableStateOf("")
    var description: String by mutableStateOf("")
    //var location: Location? by mutableStateOf(null)
    var type: Type by mutableStateOf(Type.Buildings)

    var selectedPhoto: PhotoSpot? by mutableStateOf(null)
}