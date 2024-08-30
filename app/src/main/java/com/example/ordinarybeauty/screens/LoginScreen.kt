package com.example.ordinarybeauty.screens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ordinarybeauty.viewmodels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LoginScreen(modifier: Modifier = Modifier.fillMaxSize(),
                toRegister: () -> Unit,
                toProfile: () -> Unit,
                userVM: UserViewModel = viewModel()
){
    var passShow by remember {
        mutableStateOf(false)
    }
    val auth = Firebase.auth

    Surface(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            TextField(value = userVM.username,
                onValueChange = { userVM.username = it },
                label = { Text(text = "Email")},
                isError = userVM.username.length <= 6 || !userVM.username.contains('@') || !userVM.username.endsWith(".com"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
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
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = { passShow = !passShow }) {
                Text(text = if (passShow) "Hide password" else "Show password")
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(enabled = userVM.password.length > 5 && userVM.username.length > 6 &&
                    userVM.username.contains('@') && userVM.username.endsWith(".com"),
                onClick = {
                    auth.signInWithEmailAndPassword(userVM.username, userVM.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                toProfile()
                            }
                        }
                }) {
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = {
                toRegister()
            }) {
                Text(text = "Create Account")
            }
        }
    }

}
