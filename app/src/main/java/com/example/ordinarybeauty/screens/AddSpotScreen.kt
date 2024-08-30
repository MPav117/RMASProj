package com.example.ordinarybeauty.screens

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.Type
import com.example.ordinarybeauty.data.User
import com.example.ordinarybeauty.viewmodels.PhotoSpotViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.Date

@Composable
fun AddSpotScreen(modifier: Modifier = Modifier.fillMaxSize(),
                  addVM: PhotoSpotViewModel,
                  backToMap: () -> Unit,
                  context: Context,
                  takePhoto: () -> Void?,
                  photo: Uri,
                  location: Location,
                  uploadPhoto: (type: String, folder: String, fileName: String) -> Void?){

    val auth = Firebase.auth
    val db = Firebase.firestore

    Surface(modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = AbsoluteAlignment.Right) {
                Button(onClick = { backToMap() },
                    modifier = Modifier.padding(5.dp)) {
                    Text(text = "Cancel")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    AsyncImage(model = ImageRequest.Builder(context).data(photo).build(),
                        contentDescription = "profile photo",
                        modifier = Modifier.fillMaxSize(0.8f))
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                item {
                    Button(onClick = {
                        takePhoto()
                    }){
                        Text(text = "Take Photo")
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    TextField(value = addVM.title,
                        onValueChange = { addVM.title = it },
                        label = { Text(text = "Photo Title") },
                        singleLine = true,
                        isError = addVM.title.isEmpty())
                }
                item {
                    TextField(value = addVM.description,
                        onValueChange = { addVM.description = it },
                        label = { Text(text = "Photo Description") },
                        singleLine = true)
                }
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
                item {
                    Text(text = "Photo Type")
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                item {
                    Row(horizontalArrangement = Arrangement.Center){
                        Text(text = "Buildings",
                            modifier = Modifier.padding(10.dp, 0.dp))
                        Text(text = "Nature",
                            modifier = Modifier.padding(10.dp, 0.dp))
                    }
                }
                item {
                    Row(Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.Center){
                        RadioButton(selected = addVM.type == Type.Buildings,
                            onClick = { addVM.type = Type.Buildings },
                            modifier = Modifier.padding(10.dp, 5.dp))
                        RadioButton(selected = addVM.type == Type.Nature,
                            onClick = { addVM.type = Type.Nature },
                            modifier = Modifier.padding(10.dp, 5.dp))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    Button(onClick = {
                        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                            val curUser: User? = it.toObject(User::class.java)
                            val newSpot = PhotoSpot(curUser!!.numPhotos, auth.currentUser!!.uid, addVM.title, addVM.description, location.latitude, location.longitude, addVM.type, Date(), curUser.fullname)
                            val imageKey: String = auth.currentUser!!.uid + curUser.numPhotos.toString()

                            db.collection("photoSpots").document(imageKey).set(newSpot).addOnSuccessListener {
                                db.collection("users").document(auth.currentUser!!.uid).update("numPhotos", curUser.numPhotos + 1,
                                    "points", curUser.points + 10).addOnSuccessListener {
                                    uploadPhoto("photoSpotPhotos", auth.currentUser!!.uid, imageKey)
                                    backToMap()
                                }
                            }
                        }
                    },
                        enabled = addVM.title.isNotEmpty() && photo != Uri.EMPTY) {
                        Text(text = "Create Photo Spot")
                    }
                }
            }
        }
    }
}

