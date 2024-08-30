package com.example.ordinarybeauty.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ordinarybeauty.data.User
import com.example.ordinarybeauty.viewmodels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun RegisterScreen(modifier: Modifier = Modifier.fillMaxSize(),
                   toProfile: () -> Unit,
                   backtoLogin: () -> Unit,
                   userVM: UserViewModel = viewModel(),
                   getPhoto: () -> Void?,
                   context: Context?,
                   photo: Uri,
                   uploadPhoto: (type: String, folder: String, fileName: String) -> Void?
){
    var passShow by remember {
        mutableStateOf(false)
    }

    var passCopy: String by remember {
        mutableStateOf("")
    }

    val auth = Firebase.auth
    val db = Firebase.firestore

    Surface(modifier = modifier) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
            item {
                TextField(value = userVM.username,
                    onValueChange = { userVM.username = it },
                    label = { Text(text = "Email")},
                    isError = userVM.username.length <= 6 || !userVM.username.contains('@') || !userVM.username.endsWith(".com"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            item{
                TextField(value = if (passShow) userVM.password else
                    userVM.password.replace(userVM.password, "".padEnd(userVM.password.length, '*')),
                    onValueChange = {
                        if (passShow){
                            userVM.password = it
                        } else if (it.length > userVM.password.length){
                            userVM.password += it.last()
                        } else {
                            userVM.password = userVM.password.dropLast(1)
                        }
                    },
                    label = { Text(text = "Password")},
                    isError = userVM.password.length <= 5,
                    singleLine = true
                )
            }
            item{
                TextField(value = if (passShow) passCopy else
                    passCopy.replace(passCopy, "".padEnd(passCopy.length, '*')),
                    onValueChange = {
                        if (passShow){
                            passCopy = it
                        } else if (it.length > passCopy.length){
                            passCopy += it.last()
                        } else {
                            passCopy = passCopy.dropLast(1)
                        }
                    },
                    label = { Text(text = "Copy Password")},
                    isError = passCopy.isNotEmpty() && passCopy != userVM.password,
                    singleLine = true
                )
            }
            item{
                Spacer(modifier = Modifier.height(5.dp))
            }
            item{
                Button(onClick = { passShow = !passShow }) {
                    Text(text = if (passShow) "Hide password" else "Show password")
                }
            }
            item{
                Spacer(modifier = Modifier.height(15.dp))
            }
            item{
                TextField(value = userVM.fullname,
                    onValueChange = { userVM.fullname = it },
                    label = { Text(text = "Full Name")},
                    singleLine = true,
                    isError = userVM.fullname.length < 5
                )
            }
            item{
                TextField(value = userVM.phone,
                    onValueChange = { userVM.setPhoneNumber(it) },
                    label = { Text(text = "Phone Number") },
                    singleLine = true,
                    isError = userVM.phone.length < 10
                )
            }
            item{
                Spacer(modifier = Modifier.height(5.dp))
            }
            if (photo != Uri.EMPTY){
                item{
                    AsyncImage(model = ImageRequest.Builder(context!!).data(photo).build(), contentDescription = "profile photo",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape))
                }
            }
            item {
                Button(onClick = {
                    getPhoto()
                }){
                    Text(text = "Choose Profile Photo")
                }
            }
            item{
                Spacer(modifier = Modifier.height(Dp(15f)))
            }
            item{
                Button(enabled = userVM.password.length > 5 && userVM.username.length > 6 &&
                        userVM.username.contains('@') && userVM.username.endsWith(".com") &&
                        photo != Uri.EMPTY && userVM.fullname.length >= 5 && userVM.phone.length >= 10 &&
                        userVM.password == passCopy,
                    onClick = {
                    auth.createUserWithEmailAndPassword(userVM.username, userVM.password)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful){
                                val user = User(userVM.username, userVM.password, userVM.fullname, userVM.phone)
                                db.collection("users").document(auth.currentUser!!.uid).set(user)
                                    .addOnSuccessListener {
                                        uploadPhoto("profilePhotos", auth.currentUser!!.uid, auth.currentUser!!.uid)
                                        toProfile()
                                    }
                            }
                        }
                }) {
                    Text(text = "Register")
                }
            }
            item{
                Spacer(modifier = Modifier.height(Dp(40f)))
            }
            item{
                Button(onClick = { backtoLogin() }) {
                    Text(text = "Login")
                }
            }
        }
    }

}