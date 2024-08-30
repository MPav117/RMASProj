package com.example.ordinarybeauty.screens

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.drawToBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ordinarybeauty.R
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.User
import com.example.ordinarybeauty.utils.LocationBroadcast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.Date
import androidx.compose.ui.unit.dp

@Composable
fun PhotoScreen(modifier: Modifier = Modifier.fillMaxSize(),
                backToMap: () -> Unit,
                ps: PhotoSpot?,
                context: Context,
                takePhoto: () -> Void?,
                photo: Uri,
                uploadPhoto: (type: String, folder: String, fileName: String) -> Void?
){
    val dist = FloatArray(1)

    val curloc = LocationBroadcast.curLocation

    Location.distanceBetween(ps!!.lat, ps.lng, curloc!!.latitude, curloc.longitude, dist)

    var distance: Float by remember {
        mutableFloatStateOf(dist.first())
    }

    LocationBroadcast.subscribe { l: Location ->
        Location.distanceBetween(ps.lat, ps.lng, l.latitude, l.longitude, dist)

        distance = dist.first()
    }

    val auth = Firebase.auth
    val db = Firebase.firestore

    var rating: Float by remember {
        mutableFloatStateOf(0f)
    }

    var imgUrl: Uri by remember {
        mutableStateOf(Uri.EMPTY)
    }

    val imageRef = Firebase.storage.reference.child("photoSpotPhotos/${ps!!.key}/${ps.key}${ps.id}.jpg")
    imageRef.downloadUrl.addOnSuccessListener {
        imgUrl = it
    }

    var loading: Int by remember {
        mutableIntStateOf(0)
    }

    var commented: Boolean by remember {
        mutableStateOf(false)
    }

    var rated: Boolean by remember {
        mutableStateOf(false)
    }

    val comments = Array(ps.comments.size) {Uri.EMPTY}

    fun ratePhoto(rating: Float){
        val photoKey: String = ps.key + ps.id.toString()

        db.collection("photoSpots").document(photoKey).update("rating", ((ps.rating * ps.rates.size) + rating) / (ps.rates.size + 1))
            .addOnSuccessListener {
            db.collection("photoSpots").document(photoKey).update("rates", ps.rates.plus(auth.currentUser!!.uid)).addOnSuccessListener {
                db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                    val curUser: User? = it.toObject(User::class.java)
                    db.collection("users").document(auth.currentUser!!.uid).update("points", curUser!!.points + 1).addOnSuccessListener {
                        rated = true
                    }
                }
            }
        }
    }

    if (loading == 0){
        ps.comments.forEachIndexed { i: Int, it: String ->
            if (it.startsWith(auth.currentUser!!.uid)){
                commented = true
            }
            Firebase.storage.reference.child("photoSpotComments/${ps.key}${ps.id}/${it}.jpg").downloadUrl.addOnSuccessListener {photoUrl ->
                comments[i] = photoUrl
                if (i == ps.comments.size - 1 && comments[i] != Uri.EMPTY){
                    loading = 1
                }
            }
        }
    }
    else if (loading == 1){
        loading = 2
    }

    Surface(modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = AbsoluteAlignment.Right) {
                Button(onClick = { backToMap() },
                    modifier = Modifier.padding(5.dp)) {
                    Text(text = "Back")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (imgUrl != Uri.EMPTY){
                    item {
                        AsyncImage(model = ImageRequest.Builder(context).data(imgUrl).build(),
                            contentDescription = "Photo spot photo",
                            modifier = Modifier.fillMaxSize(0.8f))
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (distance <= 20 && !rated) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { rating = 1f }) {
                                Text(text = "1")
                            }
                            Button(onClick = { rating = 2f }) {
                                Text(text = "2")
                            }
                            Button(onClick = { rating = 3f }) {
                                Text(text = "3")
                            }
                            Button(onClick = { rating = 4f }) {
                                Text(text = "4")
                            }
                            Button(onClick = { rating = 5f }) {
                                Text(text = "5")
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Button(
                            onClick = { ratePhoto(rating) },
                            enabled = rating != 0f
                        ) {
                            Text(text = if (rating != 0f) "Submit rating: ${rating.toInt()}" else "Submit rating")
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (!commented && distance <= 20){
                    item {
                        Button(onClick = { takePhoto() }) {
                            Text(text = "Take photo at this location")
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                if (photo != Uri.EMPTY && !commented){
                    item {
                        AsyncImage(model = ImageRequest.Builder(context).data(photo).build(),
                            contentDescription = "photo comment",
                            modifier = Modifier.fillMaxSize(0.8f))
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Button(onClick = {
                            db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                                val curUser: User? = it.toObject(User::class.java)
                                val photoKey: String = ps.key + ps.id.toString()
                                val newImageKey: String = auth.currentUser!!.uid + (ps.comments.lastIndex + 1).toString()

                                db.collection("photoSpots").document(photoKey).update("comments", ps.comments.plus(newImageKey),
                                    "dateLastInteraction", Date()).addOnSuccessListener {
                                    db.collection("users").document(auth.currentUser!!.uid).update("points", curUser!!.points + 3).addOnSuccessListener {
                                        uploadPhoto("photoSpotComments", photoKey, newImageKey)
                                    }
                                }
                            }
                        }) {
                            Text(text = "Submit photo")
                        }
                    }
                }
                if (imgUrl != Uri.EMPTY){
                    comments.forEach {
                        item {
                            AsyncImage(model = ImageRequest.Builder(context).data(it).build(),
                                contentDescription = "Photo spot comment",
                                modifier = Modifier.fillMaxSize(0.6f))
                        }
                    }
                }
            }
        }
    }
}