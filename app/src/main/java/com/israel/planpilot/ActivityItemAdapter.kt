package com.israel.planpilot

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.schedule.ActivityItem

class ActivityItemAdapter(context: Context, resource: Int, items: List<ActivityItem>) :
    ArrayAdapter<ActivityItem>(context, resource, items) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.list_item_activity, parent, false)

        val activityItem = getItem(position)
        val activityNameTextView = rowView.findViewById<TextView>(R.id.activityNameTextView)
        val iconImageView = rowView.findViewById<ImageView>(R.id.iconImageView)

        activityNameTextView.text = activityItem?.name
        iconImageView.setImageResource(activityItem?.iconResource ?: R.drawable.ic_default_icon)

        return rowView
    }
}
