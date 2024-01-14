package com.israel.planpilot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.israel.planpilot.Constants.LAST_DAY_OF_WEEK
import java.util.Calendar
import java.util.Date

class LargeMonCalFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var selectedDate: Date

    private inner class DayItem(val day: String, val isCurrentMonth: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        selectedDate = Calendar.getInstance().time

        val adapter = CalendarPagerAdapter()
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

            }
        )
    }

    private inner class CalendarPagerAdapter : RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_page, parent, false)

            return CalendarViewHolder(view, parent.context)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            val currentMonthDays = getDaysInYear()[position]
            holder.setDays(currentMonthDays)
        }

        override fun getItemCount(): Int {
            return 12
        }

        private inner class CalendarViewHolder(
            itemView: View,
            context: Context
        ) : RecyclerView.ViewHolder(itemView) {
            private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewDays)
            private val adapter = CalendarAdapter()

            init {
                recyclerView.layoutManager = GridLayoutManager(context, LAST_DAY_OF_WEEK)
                recyclerView.adapter = adapter

                recyclerView.addItemDecoration(HorizontalDividerItemDecoration(context))
                recyclerView.addItemDecoration(VerticalDividerItemDecoration(context))
            }

            fun setDays(newDays: List<DayItem>) {
                adapter.setDays(newDays)
            }
        }
    }

    private fun getDaysInYear(): List<List<DayItem>> {
        val daysInYear = mutableListOf<List<DayItem>>()

        for (month in 0 until 12) {
            val daysInMonth = mutableListOf<DayItem>()
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            calendar.add(Calendar.DATE, -dayOfWeek)

            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (dayOfMonth in 1..42) {

                var firstDay = dayOfMonth

                if (firstDay != calendar.get(Calendar.DAY_OF_MONTH)) {
                    firstDay = 1
                }

                val isCurrentMonth = firstDay <= lastDayOfMonth && month == calendar.get(Calendar.MONTH)

                daysInMonth.add(
                    DayItem(
                        (calendar.get(Calendar.DAY_OF_MONTH)).toString(),
                        isCurrentMonth
                    )
                )
                calendar.add(Calendar.DATE, 1)
            }

            daysInYear.add(daysInMonth)
        }

        return daysInYear
    }

    private inner class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

        private var days: List<DayItem> = emptyList()

        fun setDays(newDays: List<DayItem>) {
            val diffResult = DiffUtil.calculateDiff(DiffCallback(days, newDays))
            days = newDays
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day_large, parent, false)

            val viewPagerHeight = viewPager.measuredHeight

            val itemHeight = viewPagerHeight / 6

            val params = view.layoutParams
            params.height = itemHeight
            view.layoutParams = params

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dayItem = days[position]

            holder.textView.text = dayItem.day

            if (dayItem.isCurrentMonth) {
                holder.textView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        android.R.color.black
                    )
                )
            } else {
                holder.textView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.gray
                    )
                )
            }

            holder.textView.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Número clicado: ${dayItem.day}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount(): Int {
            return days.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.textDay)
        }
    }

    private inner class DiffCallback(
        private val oldList: List<DayItem>,
        private val newList: List<DayItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].day == newList[newItemPosition].day
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
