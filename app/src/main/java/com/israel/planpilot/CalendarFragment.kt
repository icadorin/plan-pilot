package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendar: Calendar
    private lateinit var gridView: GridView
    private lateinit var textMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        gridView = view.findViewById(R.id.gridViewDays)
        textMonthYear = view.findViewById(R.id.textMonthYear)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        btnPrevMonth.setOnClickListener { showPreviousMonth() }
        btnNextMonth.setOnClickListener { showNextMonth() }

        calendar = Calendar.getInstance()
        updateCalendar()

        return view
    }

    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textMonthYear.text = monthFormat.format(calendar.time)

        val days = getDaysInMonth()
        val adapter = CalendarCell(requireContext(), R.layout.item_calendar_day, days)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        selectedDate?.let {
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = it

            val selectedMonth = calendarSelected.get(Calendar.MONTH)
            val selectedYear = calendarSelected.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            if (selectedMonth == currentMonth && selectedYear == currentYear) {
                val selectedDay = calendarSelected.get(Calendar.DAY_OF_MONTH)

                if (selectedDay <= days.size) {
                    val selectedIndex = selectedDay + firstDayOfWeek + 2

                    adapter.setSelectedDay(selectedIndex)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun showPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateCalendar()
    }

    private fun showNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateCalendar()
    }

    private fun getDaysInMonth(): List<SpannableString> {
        val days = mutableListOf<SpannableString>()
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1

        repeat(firstDayOfWeek) {
            days.add(SpannableString(""))
        }

        repeat(lastDayOfMonth) { day ->
            val dayOfMonth = day + 1
            val isToday = isToday(cal, dayOfMonth)
            val spannableString = if (isToday) {
                highlightText(SpannableString(
                    dayOfMonth.toString()),
                    ForegroundColorSpan(Color.RED)
                )
            } else {
                SpannableString(dayOfMonth.toString())
            }

            days.add(spannableString)
        }

        return days
    }

    private fun isToday(cal: Calendar, dayOfMonth: Int): Boolean {
        val today = Calendar.getInstance()
        return (
                dayOfMonth == today.get(Calendar.DAY_OF_MONTH) &&
                        cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                )
    }

    private fun handleItemClick(position: Int) {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, position - cal.get(Calendar.DAY_OF_WEEK) + 1)
        selectedDate = cal.time

        updateCalendar()
    }

    private fun highlightText(text: SpannableString, span: Any): SpannableString {
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return text
    }

}
