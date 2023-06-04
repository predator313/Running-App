package com.aamirashraf.runningapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.aamirashraf.runningapp.other.Constants.MAP_ZOOM
import com.aamirashraf.runningapp.other.Constants.POLYLINE_COLOR
import com.aamirashraf.runningapp.other.Constants.POLYLINE_WIDTH
import com.aamirashraf.runningapp.other.TrackingUtility
import com.aamirashraf.runningapp.services.Polyline
import com.aamirashraf.runningapp.services.TrackingService
import com.aamirashraf.runningapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
//    private lateinit var mapView:MapView
    private var mapView:MapView?=null
    private var isTracking=false
    private var pathPoints= mutableListOf<Polyline>()
    private var map:GoogleMap?=null
    private var currentTimeInMillis=0L
//    private lateinit var map:GoogleMap
    lateinit var btnToggleRun:Button
    lateinit var btnFinishRun:Button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView=view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
//        val btnToggleRun=view.findViewById<Button>(R.id.btnToggleRun)
        btnToggleRun=view.findViewById(R.id.btnToggleRun)
        btnFinishRun=view.findViewById(R.id.btnFinishRun)
        btnToggleRun.setOnClickListener {
//            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            toggleRun()
        }
        mapView?.getMapAsync {
            map=it
            addAllPolylines()
        }
        subscribeToObserver()

    }
    private fun subscribeToObserver(){
        TrackingService.isTracking.observe(
            viewLifecycleOwner,
            Observer {
                updateTracking(it)
            }
        )
        TrackingService.pathPoints.observe(
            viewLifecycleOwner,
            Observer {
                pathPoints=it
                addLatestPolyline()
                moveCameraToUser()
            }
        )
        TrackingService.timeRunInMillis.observe(
            viewLifecycleOwner,
            Observer {
                currentTimeInMillis=it
                val formattedTime=TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis,true)
                val tvTimer=view?.findViewById<TextView>(R.id.tvTimer)
                tvTimer?.text=formattedTime
            }
        )
    }
    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if(!isTracking){
            //we are in pause state
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE

        }else{
            btnToggleRun.text="Stop"
            btnFinishRun.visibility=View.GONE
        }
    }
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM

                )
            )
        }else{
            val delhi=LatLng(28.56, 77.29)
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(delhi,15f)
            )
        }
    }
    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
            val preLastLatLng=pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng=pathPoints.last().last()
            val polylineOption=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOption)
        }
    }
    private fun sendCommandToService(action:String)=
        Intent(requireContext(),
            TrackingService::class.java
        ).also {
            it.action=action
            requireContext().startService(it)

        }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}