package com.israel.planpilot.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.R
import com.israel.planpilot.model.ActivityModel

class ActivityAdapter : ListAdapter<ActivityModel, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val activityName: TextView = view.findViewById(R.id.activityName)
        private val activityTime: TextView = view.findViewById(R.id.activityTime)

        fun bind(activity: ActivityModel) {
            activityName.text = activity.name
            activityTime.text = activity.alarmTriggerTime
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityModel>() {
        override fun areItemsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem == newItem
        }
    }
}

