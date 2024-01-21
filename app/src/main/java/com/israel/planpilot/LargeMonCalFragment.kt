package com.israel.planpilot

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.israel.planpilot.Constants.HORIZONTAL_CELLS
import com.israel.planpilot.Constants.VERTICAL_CELLS
import com.israel.planpilot.Constants.START_YEAR
import com.israel.planpilot.Constants.END_YEAR
import com.israel.planpilot.Constants.MONTHS_IN_YEAR
import com.israel.planpilot.Constants.TOTAL_MONTHS_IN_201_YEARS
import com.israel.planpilot.Constants.CELLS_PER_PAGE

class LargeMonCalFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var selectedDate: Date
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private inner class DayItem(
        val day: String,
        val isCurrentMonth: Boolean,
        val year: Int,
        val month: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        initViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.custom_toolbar)
    }

    private fun updateToolbar(year: Int, month: Int) {
        val adjustedMonth = month - 1

        val calendar = Calendar.getInstance()
        calendar.set(year, adjustedMonth, 1)

        val monthString = calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale.getDefault()
        )?.substring(0, 3)?.uppercase(Locale.getDefault())

        toolbar.title = "$monthString. $year"
    }

    override fun onResume() {
        super.onResume()
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        updateToolbar(year, month)
    }

    private fun initViews(view: View) {
        initWeekdayRecyclerView(view)
        initCalendarViewPager(view)
    }

    private fun initCalendarViewPager(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        selectedDate = Calendar.getInstance().time

        val startDate = Calendar.getInstance()
        val startYear = START_YEAR

        val currentYear = startDate.get(Calendar.YEAR)
        val currentMonth = startDate.get(Calendar.MONTH)

        val initialPosition = (currentYear - startYear) * MONTHS_IN_YEAR + currentMonth

        val adapter = CalendarPagerAdapter()
        viewPager.adapter = adapter

        viewPager.setCurrentItem(initialPosition, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val year = START_YEAR + position / MONTHS_IN_YEAR
                val month = position % MONTHS_IN_YEAR + 1

                Log.d("ViewPagerCallback", "onPageSelected - Posição: $position, Ano: $year, Mês: $month")

                updateToolbar(year, month)
            }
        })
    }

    private fun initWeekdayRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val weekdays = arrayOf("D", "S", "T", "Q", "Q", "S", "S")

        val adapterWeekday = WeekdayAdapter(requireContext(), weekdays)

        recyclerView.layoutManager = GridLayoutManager(context, HORIZONTAL_CELLS)
        recyclerView.adapter = adapterWeekday

        val horizontalDivider = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.HORIZONTAL
        )

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_horizontal_week_days)
            ?.let { horizontalDivider.setDrawable(it) }
        recyclerView.addItemDecoration(horizontalDivider)
    }

    private inner class CalendarPagerAdapter :
        RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

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
            return (END_YEAR - START_YEAR + 1) * MONTHS_IN_YEAR
        }

        private inner class CalendarViewHolder(
            itemView: View,
            context: Context
        ) : RecyclerView.ViewHolder(itemView) {
            private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewDays)
            private val adapter = CalendarAdapter()

            init {
                recyclerView.layoutManager = GridLayoutManager(context, HORIZONTAL_CELLS)
                recyclerView.adapter = adapter
            }

            fun setDays(newDays: List<DayItem>) {
                adapter.setDays(newDays)
            }
        }
    }

    private fun getDaysInYear(): List<List<DayItem>> {
        val daysInYear = mutableListOf<List<DayItem>>()

        for (month in 0 until TOTAL_MONTHS_IN_201_YEARS) {
            val daysInMonth = mutableListOf<DayItem>()
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            calendar.set(Calendar.YEAR, START_YEAR + month / MONTHS_IN_YEAR)
            calendar.set(Calendar.MONTH, month % MONTHS_IN_YEAR)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            calendar.add(Calendar.DATE, -dayOfWeek)

            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (dayOfMonth in 1..CELLS_PER_PAGE) {
                var firstDay = dayOfMonth

                if (firstDay != calendar.get(Calendar.DAY_OF_MONTH)) {
                    firstDay = 1
                }

                val isCurrentMonth = (firstDay <= lastDayOfMonth) &&
                        (month % MONTHS_IN_YEAR == calendar.get(Calendar.MONTH))

                daysInMonth.add(
                    DayItem(
                        (calendar.get(Calendar.DAY_OF_MONTH)).toString(),
                        isCurrentMonth,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)
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

            val itemHeight = viewPagerHeight / VERTICAL_CELLS

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

            val currentDate = Calendar.getInstance()
            val currentYear = currentDate.get(Calendar.YEAR)
            val currentMonth = currentDate.get(Calendar.MONTH)
            val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

            val itemYear = days[position].year
            val itemMonth = days[position].month
            val itemDay = dayItem.day.toInt()

            if (currentYear == itemYear && currentMonth == itemMonth && currentDay == itemDay) {
                holder.itemView.setBackgroundResource(R.color.blue_today)
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent)
            }

            holder.itemView.setOnClickListener {
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
