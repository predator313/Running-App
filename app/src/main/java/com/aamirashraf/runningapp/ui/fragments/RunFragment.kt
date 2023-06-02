package com.aamirashraf.runningapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.TrackingUtility
import com.aamirashraf.runningapp.ui.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run) {
    private val viewModel:MainViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab=view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }
//    private fun requestPermission(){
//        if(TrackingUtility.hasLocationPermission(requireContext())){
//            return
//        }
//    }
}