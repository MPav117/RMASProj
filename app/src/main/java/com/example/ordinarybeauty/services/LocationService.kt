package com.example.ordinarybeauty.services

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.ordinarybeauty.MainActivity
import com.example.ordinarybeauty.R
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.User
import com.example.ordinarybeauty.utils.LocationBroadcast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: FusedLocationProviderClient

    val db = Firebase.firestore

    override fun onCreate() {
        super.onCreate()

        locationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) {
            val notif = NotificationCompat.Builder(this, "locationservicechannel")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Tracking current location")
                    .setOngoing(true)

            serviceScope.launch {

                while (true){
                    val curLoc = locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                    LocationBroadcast.broadCast(curLoc)

                    db.collection("photoSpots").get()
                        .addOnSuccessListener { photoSpots ->
                            run loop@ {
                                photoSpots.forEach { psQ ->
                                    val ps = psQ.toObject(PhotoSpot::class.java)
                                    val location = Location("")
                                    location.latitude = ps.lat
                                    location.longitude = ps.lng

                                    if (location.distanceTo(curLoc) < 200){
                                        val i = Intent(applicationContext, MainActivity::class.java)
                                        val pi = PendingIntent.getActivity(applicationContext, 102, i, PendingIntent.FLAG_IMMUTABLE)

                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                        notificationManager.notify(2,
                                            NotificationCompat.Builder(applicationContext, "locationservicechannel")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Nearby photo spots found")
                                                .setContentText("Tap to open")
                                                .setContentIntent(pi)
                                                .build())

                                        return@loop
                                    }
                                }
                            }
                        }
                    delay(10000)
                }
            }

            startForeground(1, notif.build())
        }

        return START_STICKY
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        stopSelf()
    }

    override fun stopService(name: Intent?): Boolean {
        stop()
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }
}