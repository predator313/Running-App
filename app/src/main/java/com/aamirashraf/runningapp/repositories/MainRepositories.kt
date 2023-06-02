package com.aamirashraf.runningapp.repositories

import com.aamirashraf.runningapp.db.Run
import com.aamirashraf.runningapp.db.RunDAO
import javax.inject.Inject

class MainRepositories @Inject constructor(
    val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run)=runDAO.insertRun(run)
    suspend fun deleteRun(run:Run)=runDAO.deleteRun(run)
    fun getAllRunSortedByDate()=runDAO.getAllRunSortedByDate()
    fun getAllRunSortedByDistanceInMeters()=runDAO.getAllRunSortedByDistanceInMeters()
    fun getAllRunSortedByTimesInMillis()=runDAO.getAllRunSortedByTimeInMillis()
    fun getAllRunSortedByAvgSpeedInKMH()=runDAO.getAllRunSortedByAvgSpeedInKMH()
    fun getAllRunSortedByCaloriesBurned()=runDAO.getAllRunSortedByCaloriesBurned()


    fun getTotalAvgSpeed()=runDAO.getTotalAvgSpeed()
    fun getTotalDistance()=runDAO.getTotalDistance()
    fun getTotalCaloriesBurned()=runDAO.getTotalCaloriesBurned()
    fun getTotalTimeInMillis()=runDAO.getTotalTimeInMillis()


}