package com.example.ordinarybeauty.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.ordinarybeauty.data.Type
import java.util.Date

class FilterViewModel: ViewModel() {
    var author: String by mutableStateOf("")
    var type: Type by mutableStateOf(Type.Unspecified)
    var keyword: String by mutableStateOf("")
    var rating: Int by mutableIntStateOf(0)
    var numRates: Int by mutableIntStateOf(0)
    var startCreated: Date? by mutableStateOf(null)
    var endCreated: Date? by mutableStateOf(null)
    var startLast: Date? by mutableStateOf(null)
    var endLast: Date? by mutableStateOf(null)
    var dist: Int by mutableIntStateOf(0)

    fun setNumber(n: String, attr: Int){
        if (n.isNotEmpty() && n.isDigitsOnly()){
            when (attr){
                0 -> rating = n.toInt()
                1 -> numRates = n.toInt()
                2 -> dist = n.toInt()
            }
        }
        else if (n.isEmpty()){
            when (attr){
                0 -> rating = 0
                1 -> numRates = 0
                2 -> dist = 0
            }
        }
    }
}