package com.example.ordinarybeauty.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ordinarybeauty.data.Type
import com.example.ordinarybeauty.viewmodels.FilterViewModel
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(modifier: Modifier = Modifier.fillMaxSize(),
                 fVM: FilterViewModel,
                 back: () -> Unit
){
    val createdRangeState = rememberDateRangePickerState()
    val lastRangeState = rememberDateRangePickerState()

    var createdDialongOn: Boolean by remember {
        mutableStateOf(false)
    }

    var lastDialogOn: Boolean by remember {
        mutableStateOf(false)
    }

    if (createdDialongOn){
        DatePickerDialog(onDismissRequest = { createdDialongOn = false }, confirmButton = {
            fVM.startCreated = createdRangeState.selectedStartDateMillis?.let { Date(it) }
            fVM.endCreated = createdRangeState.selectedEndDateMillis?.let { Date(it) }
        }) {
            DateRangePicker(state = createdRangeState)
        }
    }

    if (lastDialogOn){
        DatePickerDialog(onDismissRequest = { lastDialogOn = false }, confirmButton = {
            fVM.startLast = createdRangeState.selectedStartDateMillis?.let { Date(it) }
            fVM.endLast = createdRangeState.selectedEndDateMillis?.let { Date(it) }
        }) {
            DateRangePicker(state = lastRangeState)
        }
    }
    
    Surface(modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End) {
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
                    TextField(value = fVM.author, onValueChange = { fVM.author = it }, label = { Text(text = "Author:") })
                }
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
                item { 
                    Text(text = "Photo type")
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly){
                        Text(text = "Unspecified",
                            modifier = Modifier.padding(10.dp, 0.dp))
                        Text(text = "Buildings",
                            modifier = Modifier.padding(10.dp, 0.dp))
                        Text(text = "Nature",
                            modifier = Modifier.padding(10.dp, 0.dp))
                    }
                }
                item {
                    Row(
                        Modifier
                            .selectableGroup()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly){
                        RadioButton(selected = fVM.type == Type.Unspecified, onClick = { fVM.type = Type.Unspecified })
                        RadioButton(selected = fVM.type == Type.Buildings, onClick = { fVM.type = Type.Buildings })
                        RadioButton(selected = fVM.type == Type.Nature, onClick = { fVM.type = Type.Nature })
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    TextField(value = fVM.keyword, onValueChange = { fVM.keyword = it }, label = { Text(text = "Keyword:") })
                }
                item {
                    TextField(value = fVM.rating.toString(), onValueChange = { fVM.setNumber(it, 0) }, label = { Text(text = "Min rating:") })
                }
                item {
                    TextField(value = fVM.numRates.toString(), onValueChange = { fVM.setNumber(it, 1) }, label = { Text(text = "Min number of rates:") })
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    Button(onClick = { createdDialongOn = true }) {
                        Text(text = "Pick creation date range")
                    }
                }
                item {
                    Button(onClick = { lastDialogOn = true }) {
                        Text(text = "Pick last interaction date range")
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    TextField(value = fVM.dist.toString(), onValueChange = { fVM.setNumber(it, 2) }, label = { Text(text = "Max distance:") })
                }
            }
        }
    }
}