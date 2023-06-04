package com.aamirashraf.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.aamirashraf.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.aamirashraf.runningapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.aamirashraf.runningapp.other.Constants.LOCATION_MAX_DELAY_INTERVAL
import com.aamirashraf.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_ID
import com.aamirashraf.runningapp.other.TrackingUtility
import com.aamirashraf.runningapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
typealias Polyline=MutableList<LatLng>
typealias Polylines=MutableList<Polyline>
class TrackingService:LifecycleService() {
    //how to send data through services in android
    var is_first_run=true
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        val isTracking= MutableLiveData<Boolean>()
        val pathPoints=MutableLiveData<Polylines>()
    }
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())

    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
//                    Timber.d("Started or Resume service")
                    if(is_first_run){
                        startForegroundService()
                        is_first_run=false
                    }
                    else{
                        Timber.d(" Resume service")
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stop service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermission(this)){
//                val request=LocationRequest().apply {
//                    interval= LOCATION_UPDATE_INTERVAL
//                    fastestInterval= FASTEST_LOCATION_INTERVAL
//                    priority=PRIORITY_HIGH_ACCURACY
//                }
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                    .setMaxUpdateDelayMillis(LOCATION_MAX_DELAY_INTERVAL)
                    .build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
            else{
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }
    val locationCallback=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result?.locations?.let {locations ->
                for (location in locations){
                    addPathPoints(location)
                    Timber.d("locations latitude ${location.latitude} longitude ${location.longitude}")
                }

                }
            }
        }
    }
    private fun addPathPoints(location: Location?){
        location?.let {
            val pos=LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply{
                last().add(pos)
                pathPoints.postValue(this)

            }
        }
    }
    private fun addEmptyPolyLine()= pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)

    }?: pathPoints.postValue(mutableListOf(mutableListOf()))
    private fun startForegroundService(){
        addEmptyPolyLine()
        isTracking.postValue(true)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder=NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }
    private fun getMainActivityPendingIntent()=PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainActivity::class.java).also {
            it.action=ACTION_SHOW_TRACKING_FRAGMENT
        },
//        FLAG_UPDATE_CURRENT
    FLAG_IMMUTABLE
    )
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}