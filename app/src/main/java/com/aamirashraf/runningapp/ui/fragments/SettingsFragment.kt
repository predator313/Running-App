package com.aamirashraf.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.Constants.KEY_NAME
import com.aamirashraf.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment:Fragment(R.layout.fragment_settings) {
    private lateinit var etName:EditText
    private lateinit var etWeight:EditText
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etName=view.findViewById(R.id.etName)
        etWeight=view.findViewById(R.id.etWeight)
        val btnApplyChanges=view.findViewById<Button>(R.id.btnApplyChanges)
        loadFieldsFromSharedPref()
        btnApplyChanges.setOnClickListener {
            val success=applyChangesToSharedPref()
            if(success){
                Snackbar.make(view,"saved changes",Snackbar.LENGTH_LONG).show()

            }else{
                Snackbar.make(view,"please fill out all the fields",Snackbar.LENGTH_LONG).show()
            }
        }
    }
    private fun loadFieldsFromSharedPref(){
        val name=sharedPreferences.getString(KEY_NAME,"")?:""
        val weight=sharedPreferences.getFloat(KEY_WEIGHT,80f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref():Boolean{
        val nameText=etName.text.toString()
        val weightText=etWeight.text.toString()
        if(nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weightText.toFloat())
            .apply()
        val toolbarText="let's go $nameText"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text=toolbarText
        return true
    }
}