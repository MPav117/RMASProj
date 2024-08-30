package com.example.ordinarybeauty.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {
    var username: String by mutableStateOf("")
    var password: String by mutableStateOf("")
    var fullname: String by mutableStateOf("")
    var phone: String by mutableStateOf("")

    var selectedUserId: String by mutableStateOf("")

    fun setPhoneNumber(n: String){
        if (n.isNotEmpty() && n.isDigitsOnly()){
            phone = n
        }
    }

    fun reset(){
        username = ""
        password = ""
        fullname = ""
        phone = ""
    }
}