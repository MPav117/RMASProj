package com.example.ordinarybeauty.screens

import android.annotation.SuppressLint
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ordinarybeauty.data.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@SuppressLint("UnrememberedMutableState")
@Composable
fun ListScreen(modifier: Modifier = Modifier.fillMaxSize(),
               back: () -> Unit
){
    val db = Firebase.firestore

    var users by mutableStateOf(emptyList<User>())

    db.collection("users").get()
        .addOnSuccessListener { usrs ->
            val newUsers: MutableList<User> = mutableListOf()

            usrs.forEach { usr ->
                val newUsr = usr.toObject(User::class.java)

                var i = 0
                while (i < newUsers.size && newUsr.points < newUsers[i].points){
                    i++
                }

                newUsers.add(i, newUsr)
            }

            users = newUsers
        }

    Surface(modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = AbsoluteAlignment.Right) {
                Button(onClick = { back() },
                    modifier = Modifier.padding(5.dp)) {
                    Text(text = "Back")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Name",
                            modifier = Modifier.fillMaxWidth(1/2f),
                            textAlign = TextAlign.Center)
                        VerticalDivider()
                        Text(text = "Points",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    }
                }
                users.forEach { usr ->
                    item {
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = usr.fullname,
                                modifier = Modifier.fillMaxWidth(1/2f),
                                textAlign = TextAlign.Center)
                            VerticalDivider()
                            Text(text = usr.points.toString(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}