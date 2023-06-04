package com.aamirashraf.runningapp.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import java.util.concurrent.TimeUnit

object TrackingUtility {
    fun hasLocationPermission(context:Context)=
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            //means that the device is not running in android q
            //so here the background location is by default we need not to include it
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }


    fun getFormattedStopWatchTime(ms:Long,includeMillis:Boolean=false):String{
        var millisecond=ms
        val hours=TimeUnit.MILLISECONDS.toHours(millisecond)
        millisecond-=TimeUnit.HOURS.toMillis(hours)
        val minute=TimeUnit.MILLISECONDS.toMinutes(millisecond)
        millisecond-=TimeUnit.MINUTES.toMillis(minute)
        val second=TimeUnit.MILLISECONDS.toSeconds(millisecond)
        millisecond-=TimeUnit.SECONDS.toMillis(second)
        if(!includeMillis){
            return "${if (hours<10)"0" else ""}$hours:"+
                    "${if (minute<10)"0" else ""}$minute:"+
                    "${if (second<10)"0" else ""}$second"
        }
        millisecond-=TimeUnit.SECONDS.toMillis(second)
        millisecond/=10
        return "${if (hours<10)"0" else ""}$hours:"+
                "${if (minute<10)"0" else ""}$minute:"+
                "${if (second<10)"0" else ""}$second:"+
                "${if (millisecond<10)"0" else ""}$millisecond"

    }

}