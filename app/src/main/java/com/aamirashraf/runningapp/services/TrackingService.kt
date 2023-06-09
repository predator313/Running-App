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
import com.aamirashraf.runningapp.other.Constants.TIMER_UPDATE_INTERVAL
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline=MutableList<LatLng>
typealias Polylines=MutableList<Polyline>
@AndroidEntryPoint
class TrackingService:LifecycleService() {
    //how to send data through services in android
    var is_first_run=true
    var serviceKilled=false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSeconds= MutableLiveData<Long>()
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var currentNotificationBuilder:NotificationCompat.Builder
    companion object{
        val timeRunInMillis=MutableLiveData<Long>()
        val isTracking= MutableLiveData<Boolean>()
        val pathPoints=MutableLiveData<Polylines>()
    }
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)

    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder=baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }
    private fun killService(){
        serviceKilled=true
        is_first_run=true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()

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
//                        startForegroundService()
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stop service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private var isTimerEnable=false
    private var lapTime=0L
    private var timeRun=0L
    private var timeStarted=0L
    private var lastSecondTimeStamp=0L
    private fun startTimer(){
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted=System.currentTimeMillis()
        isTimerEnable=true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //time difference b/w now and timeStarted
                lapTime=System.currentTimeMillis()-timeStarted
                timeRunInMillis.postValue(timeRun+lapTime)
                if(timeRunInMillis.value!!>=lastSecondTimeStamp+1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!!+1)
                    lastSecondTimeStamp+=1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun+=lapTime
        }
    }
    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnable=false
    }
    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText=if(isTracking)"Pause" else "Resume"
        val pendingIntent=if(isTracking){
            val pauseIntent=Intent(this,TrackingService::class.java)
                .apply {
                    action= ACTION_PAUSE_SERVICE
                }
            PendingIntent.getService(this,1,pauseIntent, FLAG_IMMUTABLE)
        }
        else{
            val resumeIntent=Intent(this,TrackingService::class.java)
                .apply {
                    action= ACTION_START_OR_RESUME_SERVICE
                }
            PendingIntent.getService(this,2,resumeIntent, FLAG_IMMUTABLE)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }

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
        startTimer()

        isTracking.postValue(true)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())
        timeRunInSeconds.observe(
            this, Observer {
                if(!serviceKilled){
                    val notification=currentNotificationBuilder
                        .setContentText(TrackingUtility.getFormattedStopWatchTime(it*1000L))
                    notificationManager.notify(NOTIFICATION_ID,notification.build())
                }

            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}