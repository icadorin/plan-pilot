package com.israel.planpilot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.israel.planpilot.Constants.CELLS_PER_PAGE
import com.israel.planpilot.Constants.END_YEAR
import com.israel.planpilot.Constants.HORIZONTAL_CELLS
import com.israel.planpilot.Constants.MONTHS_IN_YEAR
import com.israel.planpilot.Constants.START_YEAR
import com.israel.planpilot.Constants.VERTICAL_CELLS
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LargeMonCalFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var selectedDate: Date
    private lateinit var currentDate: Calendar
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private inner class DayItem(
        val day: String,
        val isCurrentMonth: Boolean,
        val year: Int,
        val month: Int
    )

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showReturnToTodayButton()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        updateToolbar(year, month)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        currentDate = Calendar.getInstance()
        initViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.custom_toolbar)
        (activity as MainActivity).showReturnToTodayButton()
        setupReturnToTodayButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).hideReturnToTodayButton()
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

                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                selectedDate = calendar.time

                updateToolbar(year, month)
            }
        })
    }

    private fun setupReturnToTodayButton() {
        val btnReturnToToday: ImageButton = toolbar.findViewById(R.id.btnReturnToToday)

        btnReturnToToday.setOnClickListener {
            selectedDate = Calendar.getInstance().time

            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val initialPosition = (currentYear - START_YEAR) * MONTHS_IN_YEAR + currentMonth

            viewPager.setCurrentItem(initialPosition, false)

            val month = currentMonth + 1
            updateToolbar(currentYear, month)
        }
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
            val year = START_YEAR + position / MONTHS_IN_YEAR
            val month = position % MONTHS_IN_YEAR + 1
            val currentMonthDays = getDaysInMonth(year, month)
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

    private fun getDaysInMonth(inputYear: Int, inputMonth: Int): List<DayItem> {
        val daysInMonth = mutableListOf<DayItem>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, inputYear)
        calendar.set(Calendar.MONTH, inputMonth - 1)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        if (firstDayOfWeek != 0) {
            calendar.add(Calendar.DATE, -firstDayOfWeek)
        }

        for (i in 1..firstDayOfWeek) {
            var prevMonth = inputMonth - 1
            var year = inputYear
            if (prevMonth < 1) {
                prevMonth += MONTHS_IN_YEAR
                year--
            }
            daysInMonth.add(
                DayItem(
                    calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    false,
                    year,
                    prevMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        calendar.set(Calendar.YEAR, inputYear)
        calendar.set(Calendar.MONTH, inputMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (dayOfMonth in 1..lastDayOfMonth) {
            daysInMonth.add(
                DayItem(
                    dayOfMonth.toString(),
                    true,
                    inputYear,
                    inputMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        val daysLeft = CELLS_PER_PAGE - daysInMonth.size
        for (i in 1..daysLeft) {
            var nextMonth = inputMonth + 1
            var year = inputYear
            if (nextMonth > MONTHS_IN_YEAR) {
                nextMonth -= MONTHS_IN_YEAR
                year++
            }
            daysInMonth.add(
                DayItem(
                    calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    false,
                    year,
                    nextMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        return daysInMonth
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

            val itemYear = days[position].year
            val itemMonth = days[position].month - 1
            val itemDay = dayItem.day.toInt()

            if (currentDate.get(Calendar.YEAR) == itemYear &&
                currentDate.get(Calendar.MONTH) == itemMonth &&
                currentDate.get(Calendar.DAY_OF_MONTH) == itemDay) {
                holder.itemView.setBackgroundResource(R.color.blue_today)
            }

            holder.itemView.setOnClickListener {
                val selectedDay = dayItem.day.toInt()

                val action = LargeMonCalFragmentDirections
                    .actionLargeMonCalFragmentToFragmentAddActivity(selectedDay)
                findNavController().navigate(action)
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
