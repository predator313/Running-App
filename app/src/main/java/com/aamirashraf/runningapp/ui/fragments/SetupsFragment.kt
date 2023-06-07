package com.aamirashraf.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.aamirashraf.runningapp.other.Constants.KEY_NAME
import com.aamirashraf.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupsFragment:Fragment(R.layout.fragment_setups) {
    private lateinit var etName:EditText
    private lateinit var etWeight:EditText
    @Inject
    lateinit var sharedPref:SharedPreferences

    //in case of primitive datatype we should use the @set:Inject
    //as we cant use lateint var with the primitive datatypes
    @set:Inject
    var isFirstAppOpen=true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvContinue=view.findViewById<TextView>(R.id.tvContinue)
        etName=view.findViewById(R.id.etName)
        etWeight=view.findViewById(R.id.etWeight)
        if(!isFirstAppOpen){
            val navOptions=NavOptions.Builder()
                .setPopUpTo(R.id.setupsFragment,true)
                .build()
            findNavController().navigate(
                R.id.action_setupsFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }
        tvContinue.setOnClickListener {
            val success=writePersonalDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setupsFragment_to_runFragment)
            }
            else{
                Snackbar.make(requireView(),"please enter all the fields",Snackbar.LENGTH_LONG).show()
            }
        }

    }
    private fun writePersonalDataToSharedPref():Boolean{
        val name=etName.text.toString()
        val weight=etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolbarText="let's go , $name "
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text=toolbarText
        return true

    }
}