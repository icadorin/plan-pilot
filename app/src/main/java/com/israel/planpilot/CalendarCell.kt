package com.israel.planpilot

import android.content.Context
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat

class CalendarCell(context: Context, resource: Int, objects: List<SpannableString>) :
    ArrayAdapter<SpannableString>(context, resource, objects) {

    private var selectedDay = -1

    fun setSelectedDay(day: Int) {
        selectedDay = day
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        if (position == selectedDay) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.circle)
            view.background = drawable
        } else {
            view.background = null
        }

        return view
    }
}

