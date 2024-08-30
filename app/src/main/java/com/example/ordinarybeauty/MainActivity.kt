package com.example.ordinarybeauty

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.request.ImageRequest
import com.example.ordinarybeauty.screens.AddSpotScreen
import com.example.ordinarybeauty.screens.FilterScreen
import com.example.ordinarybeauty.screens.ListScreen
import com.example.ordinarybeauty.screens.LoginScreen
import com.example.ordinarybeauty.screens.MapScreen
import com.example.ordinarybeauty.screens.PhotoScreen
import com.example.ordinarybeauty.screens.ProfileScreen
import com.example.ordinarybeauty.screens.RegisterScreen
import com.example.ordinarybeauty.screens.TableScreen
import com.example.ordinarybeauty.services.LocationService
import com.example.ordinarybeauty.ui.theme.OrdinaryBeautyTheme
import com.example.ordinarybeauty.viewmodels.FilterViewModel
import com.example.ordinarybeauty.viewmodels.PhotoSpotViewModel
import com.example.ordinarybeauty.viewmodels.UserViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {

    private var photoUri: Uri by mutableStateOf(Uri.EMPTY)
    private var cameraUri: Uri = Uri.EMPTY

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri !== null) {
            photoUri = uri
        }
    }
    private val photoTaker = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            photoUri = cameraUri
        }
    }
    private var i: Intent? = null
    private var isStarted: Boolean by mutableStateOf(false)
    private var newSpotLocation: Location? = null

    private val locationObserver = MyReciever(::locationTurnedOn, ::locationTurnedOff)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrdinaryBeautyTheme {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    OBApp(Modifier.fillMaxSize())
                }

                NavBar(status = isStarted, turnOn = ::locationTurnedOn, turnOff = ::locationTurnedOff)
            }
        }

        getPerms()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val locReq = LocationRequest.Builder(30000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locReq)

            val task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                locationTurnedOn()
            }
                .addOnFailureListener { e ->
                    (e as ResolvableApiException).startResolutionForResult(this, 100)
                }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        registerReceiver(locationObserver, filter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(locationObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopService(i)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Composable
    fun OBApp(modifier: Modifier = Modifier.fillMaxSize()){
        val userVM: UserViewModel = viewModel()
        val addVM: PhotoSpotViewModel = viewModel()
        val filterVM: FilterViewModel = viewModel()
        val navCon = rememberNavController()

        NavHost(navController = navCon, startDestination = Screens.Login.name) {
            composable(Screens.Login.name){
                LoginScreen(modifier,
                    toRegister = {
                        userVM.reset()
                        navCon.navigate(Screens.Register.name) },
                    toProfile = { navCon.navigate((Screens.Profile.name)) },
                    userVM)
            }
            composable(Screens.Register.name){
                RegisterScreen(modifier,
                    toProfile = { navCon.navigate((Screens.Profile.name)) },
                    backtoLogin = {
                        userVM.reset()
                        navCon.popBackStack() },
                    userVM,
                    { getPhoto() },
                    applicationContext,
                    photoUri,
                    ::uploadPhoto)
            }
            composable(Screens.Profile.name){
                ProfileScreen(modifier,
                    toMap = {navCon.navigate(Screens.Map.name)},
                    toSpot = {
                        addVM.selectedPhoto = it
                        photoUri = Uri.EMPTY
                        navCon.navigate(Screens.Photo.name)
                    },
                    toTable = {navCon.navigate(Screens.Table.name)},
                    toList = {navCon.navigate(Screens.List.name)},
                    applicationContext)
            }
            composable(Screens.Map.name){
                MapScreen(modifier,
                    toPhoto = {
                        addVM.selectedPhoto = it
                        photoUri = Uri.EMPTY
                        navCon.navigate(Screens.Photo.name )},
                    toAdd = {
                        newSpotLocation = it
                        photoUri = Uri.EMPTY
                        navCon.navigate(Screens.Add.name) },
                    filterVM = filterVM,
                    toFilter = { navCon.navigate(Screens.Filter.name) }
                )
            }
            composable(Screens.List.name){
                ListScreen(modifier,
                    back = { navCon.popBackStack() })
            }
            composable(Screens.Photo.name){
                PhotoScreen(modifier,
                    backToMap = { navCon.popBackStack() },
                    ps = addVM.selectedPhoto,
                    context = applicationContext,
                    takePhoto = { takePhoto() },
                    photo = photoUri,
                    ::uploadPhoto
                    )
            }
            composable(Screens.Add.name){
                AddSpotScreen(modifier,
                    addVM = addVM,
                    backToMap = { navCon.popBackStack() },
                    applicationContext,
                    takePhoto = { takePhoto() },
                    photo = photoUri,
                    location = newSpotLocation!!,
                    ::uploadPhoto)
            }
            composable(Screens.Filter.name){
                FilterScreen(modifier,
                    filterVM,
                    back = { navCon.popBackStack() })
            }
            composable(Screens.Table.name){
                TableScreen(modifier,
                    filterVM,
                    back = { navCon.popBackStack() },
                    toFilter = { navCon.navigate(Screens.Filter.name) },
                    toPhoto = {
                        addVM.selectedPhoto = it
                        photoUri = Uri.EMPTY
                        navCon.navigate(Screens.Photo.name )})
            }
        }
    }

    private fun getPhoto(): Void? {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        return null
    }

    @SuppressLint("SimpleDateFormat")
    private fun takePhoto(): Void? {
        val fileName: String = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Date())
        val photo = File.createTempFile(fileName, ".jpg", this.externalCacheDir)
        cameraUri = FileProvider.getUriForFile(this,"com.example.ordinarybeauty.provider", photo)
        photoTaker.launch(cameraUri)
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun uploadPhoto(type: String, folder: String, fileName: String): Void? {
        val imageref: StorageReference = Firebase.storage.reference.child(type)
            .child(folder).child("${fileName}.jpg")
        val baos = ByteArrayOutputStream()
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(applicationContext.contentResolver, photoUri))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        imageref.putBytes(data)
        return null
    }

    private fun getPerms(){
        val requestPerms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPerms.launch(arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.CAMERA
            ))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPerms.launch(arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.CAMERA
            ))
        } else {
            requestPerms.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            ))
        }
    }

    private fun locationTurnedOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            i = Intent(applicationContext, LocationService::class.java)
            startForegroundService(i)
            isStarted = true
        }
    }

    private fun locationTurnedOff(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isStarted) {
            stopService(i)
            isStarted = false
        }
    }

    @Composable
    fun NavBar(status: Boolean, turnOn: () -> Unit, turnOff: () -> Unit){
        val func = if (status) turnOff else turnOn
        Button(onClick = func,
            modifier = Modifier.padding(5.dp)) {
            val txt = if (status) "Tracking: On" else "Tracking: Off"
            Text(text = txt)
        }
    }
}

enum class Screens {
    Login,
    Register,
    Profile,
    Map,
    Photo,
    List,
    Add,
    Filter,
    Table
}

class MyReciever(private val on: () -> Unit, private val off: () -> Unit): BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == LocationManager.MODE_CHANGED_ACTION){
            val locMan = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                on()
            }
            else {
                off()
            }
        }
    }
}