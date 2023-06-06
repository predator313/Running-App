package com.aamirashraf.runningapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aamirashraf.runningapp.db.Run
import com.aamirashraf.runningapp.other.SortType
import com.aamirashraf.runningapp.repositories.MainRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepositories: MainRepositories
):ViewModel(){
   private val runSortedByDate=mainRepositories.getAllRunSortedByDate()
   private val runSortedByDistance=mainRepositories.getAllRunSortedByDistanceInMeters()
   private val runSortedByCaloriesBurned=mainRepositories.getAllRunSortedByCaloriesBurned()
    private val runSortedByTimeInMillis=mainRepositories.getAllRunSortedByTimesInMillis()
   private val runSortedByAvgSpeed=mainRepositories.getAllRunSortedByAvgSpeedInKMH()

    //mediator livedata is very important
    val runs=MediatorLiveData<List<Run>>()
    var sortType=SortType.DATE
    init {
        runs.addSource(runSortedByDate){result ->
            if(sortType==SortType.DATE){
                result?.let {
                    runs.value=it
                }
            }

        }
        runs.addSource(runSortedByAvgSpeed){result ->
            if(sortType==SortType.AVG_SPEED){
                result?.let {
                    runs.value=it
                }
            }

        }
        runs.addSource(runSortedByCaloriesBurned){result ->
            if(sortType==SortType.CALORIES_BURNED){
                result?.let {
                    runs.value=it
                }
            }

        }
        runs.addSource(runSortedByDistance){result ->
            if(sortType==SortType.DISTANCE){
                result?.let {
                    runs.value=it
                }
            }

        }
        runs.addSource(runSortedByTimeInMillis){result ->
            if(sortType==SortType.RUNNING_TIME){
                result?.let {
                    runs.value=it
                }
            }

        }
    }
    fun sortRuns(sortType: SortType)=when(sortType){
        SortType.DATE -> runSortedByDate.value?.let { runs.value=it }
        SortType.RUNNING_TIME->runSortedByTimeInMillis.value?.let { runs.value=it }
        SortType.DISTANCE->runSortedByDistance.value?.let { runs.value=it }
        SortType.CALORIES_BURNED->runSortedByCaloriesBurned.value?.let { runs.value=it }
        SortType.AVG_SPEED->runSortedByAvgSpeed.value?.let { runs.value=it }
    }.also {
        this.sortType=sortType
    }
    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepositories.insertRun(run)
    }
}