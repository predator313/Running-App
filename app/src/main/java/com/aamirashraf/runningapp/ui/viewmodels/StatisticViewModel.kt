package com.aamirashraf.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.aamirashraf.runningapp.repositories.MainRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class StatisticViewModel @Inject constructor(
    val mainRepositories: MainRepositories
):ViewModel(){
    val totalTimeRun=mainRepositories.getTotalTimeInMillis()
    val totalDistance=mainRepositories.getTotalDistance()
    val totalCaloriesBurned=mainRepositories.getTotalCaloriesBurned()
    val totalAvgSpeed=mainRepositories.getTotalAvgSpeed()

    val runSortedByDate=mainRepositories.getAllRunSortedByDate()
}