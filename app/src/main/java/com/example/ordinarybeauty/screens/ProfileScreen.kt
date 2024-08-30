package com.example.ordinarybeauty.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

@Composable
fun ProfileScreen(modifier: Modifier = Modifier.fillMaxSize(),
                  toMap: () -> Unit,
                  toSpot: (ps: PhotoSpot) -> Unit,
                  toTable: () -> Unit,
                  toList: () -> Unit,
                  context: Context?
){
    val auth = Firebase.auth
    val db = Firebase.firestore

    var loading: Int by remember {
        mutableIntStateOf(0)
    }

    var curUsr: User? by remember {
        mutableStateOf(null)
    }

    var pfpUrl: Uri by remember {
        mutableStateOf(Uri.EMPTY)
    }

    var userSpots: Array<PhotoSpot> by remember {
        mutableStateOf(emptyArray())
    }

    var photoUrls: Array<Uri> by remember {
        mutableStateOf(emptyArray())
    }

    val pfpRef = Firebase.storage.reference.child("profilePhotos/${auth.currentUser!!.uid}/${auth.currentUser!!.uid}.jpg")

    if (loading == 0){
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            curUsr = it.toObject(User::class.java)
            pfpRef.downloadUrl.addOnSuccessListener { url: Uri ->
                pfpUrl = url
                db.collection("photoSpots").where(Filter.equalTo("key", auth.currentUser!!.uid)).get().addOnSuccessListener { spots ->
                    userSpots = spots.toObjects(PhotoSpot::class.java).toTypedArray()
                    photoUrls = Array(curUsr!!.numPhotos) {Uri.EMPTY}
                    userSpots.forEachIndexed { i: Int, ps: PhotoSpot ->
                        Firebase.storage.reference.child("photoSpotPhotos/${ps.key}/${ps.key}${ps.id}.jpg").downloadUrl.addOnSuccessListener {photoUrl ->
                            photoUrls[i] = photoUrl
                            if (i == userSpots.size - 1 && photoUrls[i] != Uri.EMPTY){
                                loading = 1
                            }
                        }
                    }
                }
            }
        }
    }
    else if (loading == 1){
        loading = 2
    }

    Surface(modifier = modifier) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
            item {
                AsyncImage(model = ImageRequest.Builder(context!!).data(pfpUrl).build(),
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape))
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                curUsr?.let { Text(text = it.fullname) }
            }
            item{
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                curUsr?.let { Text(text = "Points: ${curUsr!!.points}") }
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { toMap() }) {
                        Text(text = "Map")
                    }
                    Button(onClick = { toTable() }) {
                        Text(text = "Table")
                    }
                    Button(onClick = { toList() }) {
                        Text(text = "List")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(15.dp))
            }
            photoUrls.forEachIndexed { i: Int, it: Uri ->
                item {
                    Surface(modifier = Modifier.fillMaxWidth(0.7f),
                        onClick = { toSpot(userSpots[i]) }
                        ) {
                        AsyncImage(model = ImageRequest.Builder(context!!).data(it).build(),
                            contentDescription = "Photo spot photo")
                    }

                }
            }
        }
    }
}