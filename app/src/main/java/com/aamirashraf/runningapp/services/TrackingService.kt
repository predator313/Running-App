package com.aamirashraf.runningapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.aamirashraf.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.aamirashraf.runningapp.other.Constants.NOTIFICATION_ID
import com.aamirashraf.runningapp.ui.MainActivity
import timber.log.Timber

class TrackingService:LifecycleService() {
    //how to send data through services in android
    var is_first_run=true;

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
    private fun startForegroundService(){
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
    FLAG_MUTABLE
    )
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}