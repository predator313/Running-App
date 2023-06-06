package com.aamirashraf.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aamirashraf.runningapp.db.Run
import com.aamirashraf.runningapp.repositories.MainRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepositories: MainRepositories
):ViewModel(){
    val runSortedByDate=mainRepositories.getAllRunSortedByDate()
    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepositories.insertRun(run)
    }
}