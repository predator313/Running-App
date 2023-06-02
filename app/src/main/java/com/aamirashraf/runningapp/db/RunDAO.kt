package com.aamirashraf.runningapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)
    @Delete
    suspend fun deleteRun(run: Run)

    @Query("select * from running_table order by timestamp desc")
    fun getAllRunSortedByDate():LiveData<List<Run>>
    @Query("select * from running_table order by timeInMillis desc")
    fun getAllRunSortedByTimeInMillis():LiveData<List<Run>>
    @Query("select * from running_table order by caloriesBurned desc")
    fun getAllRunSortedByCaloriesBurned():LiveData<List<Run>>
    @Query("select * from running_table order by avgSpeedInKMH desc")
    fun getAllRunSortedByAvgSpeedInKMH():LiveData<List<Run>>
    @Query("select * from running_table order by distanceInMeters desc")
    fun getAllRunSortedByDistanceInMeters():LiveData<List<Run>>

    @Query("select sum(timeInMillis) from running_table")
    fun getTotalTimeInMillis():LiveData<Long>
    @Query("select sum(caloriesBurned) from running_table")
    fun getTotalCaloriesBurned():LiveData<Int>
    @Query("select sum(distanceInMeters) from running_table")
    fun getTotalDistance():LiveData<Int>
    @Query("select avg(avgSpeedInKMH) from running_table")
    fun getTotalAvgSpeed():LiveData<Float>
}