package com.aamirashraf.runningapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.db.Run
import com.aamirashraf.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.aamirashraf.runningapp.other.Constants.ACTION_STOP_SERVICE
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mapView:MapView
//    private var mapView:MapView?=null
    private var isTracking=false
    private var pathPoints= mutableListOf<Polyline>()
    private var map:GoogleMap?=null
    private var currentTimeInMillis=0L
//    private lateinit var map:GoogleMap
    private var menu:Menu?=null
    lateinit var btnToggleRun:Button
    lateinit var btnFinishRun:Button
    private var weight=80f
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //for fragment
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView=view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
//        val btnToggleRun=view.findViewById<Button>(R.id.btnToggleRun)
        btnToggleRun=view.findViewById(R.id.btnToggleRun)
        btnFinishRun=view.findViewById(R.id.btnFinishRun)
        btnToggleRun.setOnClickListener {
//            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            toggleRun()
        }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        mapView.getMapAsync {
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
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu=menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMillis>0){
            this.menu?.getItem(0)?.isVisible=true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showCancelTrackingDialog(){
        val dialog=MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("cancel the Run??")
            .setMessage("Are you sure to cancel the current Run and delete all its data")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                stopRun()

            }
            .setNegativeButton("No"){ dialogInterface,_ ->
                dialogInterface.cancel()
            }.create()
        dialog.show()
    }
    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if(!isTracking){
            //we are in pause state
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE

        }else{
            btnToggleRun.text="Stop"
            menu?.getItem(0)?.isVisible=true
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
    private fun zoomToSeeWholeTrack(){
        val bounds=LatLngBounds.builder()
        for(ployline in pathPoints){
            for(pos in ployline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height* 0.05f).toInt()

            )
        )
    }
    private fun endRunAndSaveToDb(){
        map?.snapshot {bmp ->
            var distanceInMeters=0
            for (polyline in pathPoints){
                distanceInMeters+=TrackingUtility.calculatePolyLineLength(
                    polyline
                ).toInt()
            }
            val avgSpeed= round((distanceInMeters/1000f) /(currentTimeInMillis/1000f/60/60)*10)/10f
            val dateTimestamp=Calendar.getInstance().timeInMillis
            val caloriesBurned=((distanceInMeters/1000f) * weight).toInt()
            val run=Run(bmp,dateTimestamp,avgSpeed,distanceInMeters,currentTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
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