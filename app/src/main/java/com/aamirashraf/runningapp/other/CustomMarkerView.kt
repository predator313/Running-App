package com.aamirashraf.runningapp.other

import android.content.Context
import android.widget.TextView
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomMarkerView(
    val runs:List<Run>,
    context: Context,
    layoutId:Int
):MarkerView(context,layoutId) {
    private val tvDate=findViewById<TextView>(R.id.tvDate)
    private val tvAvgSpeed=findViewById<TextView>(R.id.tvAvgSpeed)
    private val tvDistance=findViewById<TextView>(R.id.tvDistance)
    private val tvDuration=findViewById<TextView>(R.id.tvDuration)
    private val tvCaloriesBurned=findViewById<TextView>(R.id.tvCaloriesBurned)
    override fun getOffset(): MPPointF {
//        return super.getOffset()
        return MPPointF(-width/2f,-height.toFloat())
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e==null){
            return
        }
        val currentRunId=e.x.toInt()//index
        val run=runs[currentRunId]

        val calendar= Calendar.getInstance().apply {
            timeInMillis=run.timestamp
        }
        val dateFormat= SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text=dateFormat.format(calendar.time)
        val avgSpeed="${run.avgSpeedInKMH}Km/h"
        tvAvgSpeed.text=avgSpeed
        val distanceInKm="${run.distanceInMeters/1000f}km"
        tvDistance.text=distanceInKm
        tvDuration.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
        val caloriesBurned="${run.caloriesBurned}kcal"
        tvCaloriesBurned.text=caloriesBurned
    }
}