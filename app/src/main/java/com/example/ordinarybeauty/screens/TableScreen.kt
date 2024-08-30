package com.example.ordinarybeauty.screens

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.utils.LocationBroadcast
import com.example.ordinarybeauty.utils.checkFilter
import com.example.ordinarybeauty.viewmodels.FilterViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@SuppressLint("UnrememberedMutableState")
@Composable
fun TableScreen(modifier: Modifier = Modifier.fillMaxSize(),
                filterVM: FilterViewModel,
                back: () -> Unit,
                toFilter: () -> Unit,
                toPhoto: (ps: PhotoSpot) -> Unit
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
            val newSpots: MutableList<PhotoSpot> = mutableListOf()
            pssQ.forEach { psQ ->
                val ps = psQ.toObject(PhotoSpot::class.java)

                if (curLoc != null && checkFilter(filterVM, ps, curLoc!!)){
                    newSpots.add(ps)
                }
            }
            photoSpots = newSpots
        }
    
    Surface(modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = AbsoluteAlignment.Right) {
                Row(modifier = Modifier.padding(5.dp)) {
                    Button(onClick = { toFilter() }) {
                        Text(text = "Filter")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = { back() }) {
                        Text(text = "Back")
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth()){
                        Text(text = "Author",
                            modifier = Modifier.fillMaxWidth(1/3f),
                            textAlign = TextAlign.Center)
                        VerticalDivider()
                        Text(text = "Photo Name",
                            modifier = Modifier.fillMaxWidth(1/2f),
                            textAlign = TextAlign.Center)
                        VerticalDivider()
                        Text(text = "Rating",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    }
                }
                photoSpots.forEach { ps ->
                    item {
                        HorizontalDivider()
                        Surface(modifier = Modifier.fillMaxWidth(),
                            onClick = { toPhoto(ps) }) {
                            Row(modifier = Modifier.fillMaxWidth()){
                                Text(text = ps.authorName,
                                    modifier = Modifier.fillMaxWidth(1/3f),
                                    textAlign = TextAlign.Center)
                                VerticalDivider()
                                Text(text = ps.title,
                                    modifier = Modifier.fillMaxWidth(1/2f),
                                    textAlign = TextAlign.Center)
                                VerticalDivider()
                                Text(text = String.format("%.1f", ps.rating),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}