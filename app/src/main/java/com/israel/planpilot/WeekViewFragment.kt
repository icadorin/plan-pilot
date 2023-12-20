package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WeekViewFragment : Fragment() {

    private lateinit var calendarRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_week_view, container, false)

        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        calendarRecyclerView.layoutManager = layoutManager

        val adapter = WeekDaysAdapter()
        calendarRecyclerView.adapter = adapter

        return view
    }
    private class WeekDaysAdapter : RecyclerView.Adapter<WeekDaysAdapter.DayViewHolder>() {

        private val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            view.textSize = 16f
            view.textAlignment = View.TEXT_ALIGNMENT_CENTER
            return DayViewHolder(view)
        }
        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            val day = daysOfWeek[position]
            holder.bind(day)
        }
        override fun getItemCount(): Int {
            return daysOfWeek.size
        }
        class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayTextView: TextView = itemView as TextView

            fun bind(day: String) {
                dayTextView.text = day
            }
        }
    }
}




