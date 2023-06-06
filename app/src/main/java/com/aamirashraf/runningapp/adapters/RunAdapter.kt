package com.aamirashraf.runningapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aamirashraf.runningapp.R
import com.aamirashraf.runningapp.db.Run
import com.aamirashraf.runningapp.other.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter:RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var ivRunImage=itemView.findViewById<ImageView>(R.id.ivRunImage)
        var tvDate=itemView.findViewById<TextView>(R.id.tvDate)
        var tvDistance=itemView.findViewById<TextView>(R.id.tvDistance)
        var tvAvgSpeed=itemView.findViewById<TextView>(R.id.tvAvgSpeed)
        var tvTime=itemView.findViewById<TextView>(R.id.tvTime)
        var tvCalories=itemView.findViewById<TextView>(R.id.tvCalories)
    }
    val diffCallback=object :DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
           return oldItem.hashCode()==newItem.hashCode()
        }

    }
    private var differ=AsyncListDiffer(this,diffCallback)
    fun submitList(list: List<Run>)=differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_run,parent,false
                )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
       val run=differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(
                run.bitmap
            ).into(holder.ivRunImage)
            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }
            val dateFormat=SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            holder.tvDate.text=dateFormat.format(calendar.time)
            val avgSpeed="${run.avgSpeedInKMH}Km/h"
            holder.tvAvgSpeed.text=avgSpeed
            val distanceInKm="${run.distanceInMeters/1000f}km"
            holder.tvDistance.text=distanceInKm
            holder.tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
            val caloriesBurned="${run.caloriesBurned}kcal"
            holder.tvCalories.text=caloriesBurned
        }
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }
}