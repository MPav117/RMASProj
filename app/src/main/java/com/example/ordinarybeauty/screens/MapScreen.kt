package com.example.ordinarybeauty.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.drawToBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.User
import com.example.ordinarybeauty.utils.LocationBroadcast
import com.example.ordinarybeauty.utils.checkFilter
import com.example.ordinarybeauty.viewmodels.FilterViewModel
import com.example.ordinarybeauty.viewmodels.PhotoSpotViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.storage
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(modifier: Modifier = Modifier.fillMaxSize(),
              toPhoto: (PhotoSpot) -> Unit,
              toAdd: (l: Location) -> Unit,
              filterVM: FilterViewModel,
              toFilter: () -> Unit
){
    var curLoc: Location? by remember {
        mutableStateOf(LocationBroadcast.curLocation)
    }

    LocationBroadcast.subscribe { l: Location ->
        curLoc = l
    }

    val db = Firebase.firestore

    var photoSpots by mutableStateOf(emptyList<PhotoSpot>())

    db.collection("photoSpots").get()
        .addOnSuccessListener { pssQ ->
            val newSpots:MutableList<PhotoSpot> = mutableListOf()
            pssQ.forEach { psQ ->
                Log.d("KOBE MOVE", psQ.toString())
                val ps = psQ.toObject(PhotoSpot::class.java)
                Log.d("PLEASE BRO", ps.toString())

                if (curLoc != null && checkFilter(filterVM, ps, curLoc!!)){
                    newSpots.add(ps)
                }
            }
            photoSpots = newSpots
        }

    val cameraPositionState = rememberCameraPositionState{
        position = if (curLoc !== null){
            CameraPosition.fromLatLngZoom(LatLng(curLoc!!.latitude, curLoc!!.longitude), 15f)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
        }
    }
    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true)) }
    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }

    Surface(modifier = modifier) {
        Column(horizontalAlignment = Alignment.End) {
            Row(modifier = Modifier.padding(5.dp)) {
                Button(onClick = { toFilter() }) {
                    Text(text = "Filter")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = { if (curLoc != null) {
                    toAdd(curLoc!!)
                }
                }) {
                    Text(text = "Take photo")
                }
            }
            GoogleMap(modifier = modifier,
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = properties
            ){
                if (curLoc !== null){
                    Circle(center = LatLng(curLoc!!.latitude, curLoc!!.longitude),
                        fillColor = Color.White,
                        radius = 10.0,
                        strokeColor = Color.Blue
                    )
                }
                for (ps in photoSpots) {
                    Marker(state = MarkerState(LatLng(ps.lat, ps.lng)),
                        title = ps.title,
                        snippet = ps.description,
                        onInfoWindowClick = { toPhoto(ps) }
                    )
                }
            }
        }
    }

}