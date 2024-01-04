package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Date

import com.israel.planpilot.Constants.LAST_DAY_OF_WEEK

class LargeMonCalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var selectedDate: Date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        initViews(view)
        updateCalendar()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewDays)
        selectedDate = Calendar.getInstance().time

        layoutManager = GridLayoutManager(activity, LAST_DAY_OF_WEEK)
        recyclerView.layoutManager = layoutManager

        val verticalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = true)
        val horizontalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = false)

        recyclerView.addItemDecoration(verticalDivider)
        recyclerView.addItemDecoration(horizontalDivider)

        recyclerView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                    val screenHeight = recyclerView.height
                    val days = getDaysInMonth()
                    val adapter = CalendarAdapter(days, screenHeight)
                    recyclerView.adapter = adapter

                    return true
                }
            }
        )
    }

    private fun updateCalendar() {
        //
    }

    private fun getDaysInMonth(): List<String> {
        val days = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (dayOfMonth in 1..lastDayOfMonth) {
            days.add(dayOfMonth.toString())
        }

        return days
    }

    private inner class CalendarAdapter(
        private val days: List<String>,
        private val screenHeight: Int
    ) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_calendar_day_large,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return days.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = days[position]

            val numberOfVisibleCells = 5
            val adjustedHeight = screenHeight / numberOfVisibleCells

            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = adjustedHeight
            holder.itemView.layoutParams = layoutParams
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.textDay)
        }
    }
}

