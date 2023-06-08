package com.aamirashraf.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.other.CustomMarkerView
import com.aamirashraf.runningapp.other.TrackingUtility
import com.aamirashraf.runningapp.ui.viewmodels.MainViewModel
import com.aamirashraf.runningapp.ui.viewmodels.StatisticViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticFragment:Fragment(R.layout.fragment_statistic) {
    private val viewModel: StatisticViewModel by viewModels()
    private lateinit var tvTotalTime:TextView
    private lateinit var tvTotalDistance:TextView
    private lateinit var tvAvgSpeed:TextView
    private lateinit var tvTotalCalories:TextView
    private lateinit var barChart: BarChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTotalTime=view.findViewById(R.id.tvTotalTime)
        tvTotalDistance=view.findViewById(R.id.tvTotalDistance)
        tvAvgSpeed=view.findViewById(R.id.tvAverageSpeed)
        tvTotalCalories=view.findViewById(R.id.tvTotalCalories)
        barChart=view.findViewById(R.id.barChart)
        subscribeToObserver()
        setupBarChart()
    }
    private fun setupBarChart(){
        barChart.xAxis.apply {
            position= XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor=Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor=Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text="Avg Speed Over Time"
            legend.isEnabled=false
        }
    }
    private fun subscribeToObserver(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun=TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text=totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km=it/1000f
                val totalDistance= round(km*10f)/10f
                val totalDistanceString="${totalDistance}km"
                tvTotalDistance.text=totalDistanceString

            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed= round(it*10f)/10f
                val avgSpeedString="${avgSpeed}km/h"
                tvAvgSpeed.text=avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories="${it}kcal"
                tvTotalCalories.text=totalCalories
            }
        })
        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed=it.indices.map { i ->BarEntry(i.toFloat(),it[i].avgSpeedInKMH)}
                val barDataSet=BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor=Color.WHITE
                    color=ContextCompat.getColor(requireContext(),R.color.colorAccent)

                }
                barChart.data= BarData(barDataSet)
                barChart.marker=CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart.invalidate()

            }
        })
    }
}