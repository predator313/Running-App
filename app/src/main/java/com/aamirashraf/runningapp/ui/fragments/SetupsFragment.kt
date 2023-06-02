package com.aamirashraf.runningapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aamirashraf.runningapp.R

class SetupsFragment:Fragment(R.layout.fragment_setups) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvContinue=view.findViewById<TextView>(R.id.tvContinue)
        tvContinue.setOnClickListener {
            findNavController().navigate(R.id.action_setupsFragment_to_runFragment)
        }

    }
}