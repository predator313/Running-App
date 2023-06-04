package com.aamirashraf.runningapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.databinding.ActivityMainBinding
import com.aamirashraf.runningapp.db.RunDAO
import com.aamirashraf.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.aamirashraf.runningapp.ui.fragments.TrackingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
//    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    navigateToTrackingFragmentIfNeeded(intent)
//         navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
       val  navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)

        binding.bottomNavigationView.setupWithNavController(navHostFragment!!.findNavController())
        navHostFragment.findNavController().addOnDestinationChangedListener{ _, destination, _ ->
            when(destination.id){
                R.id.settingsFragment,R.id.runFragment,R.id.statisticFragment ->
                    binding.bottomNavigationView.visibility=View.VISIBLE
                else ->binding.bottomNavigationView.visibility=View.GONE
            }

        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action==ACTION_SHOW_TRACKING_FRAGMENT){
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
//
            navHostFragment!!.findNavController().navigate(R.id.action_global_tracing_fragment)

//
        }
    }
}