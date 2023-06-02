package com.aamirashraf.runningapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.ui.viewmodels.MainViewModel
import com.aamirashraf.runningapp.ui.viewmodels.StatisticViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticFragment:Fragment(R.layout.fragment_statistic) {
    private val viewModel: StatisticViewModel by viewModels()
}