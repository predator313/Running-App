package com.aamirashraf.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.aamirashraf.runningapp.repositories.MainRepositories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepositories: MainRepositories
):ViewModel(){
}