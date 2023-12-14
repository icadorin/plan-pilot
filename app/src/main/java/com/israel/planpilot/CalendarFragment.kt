package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

        val days = getDaysInMonth().toMutableList()
        val adapter = ArrayAdapter(requireContext(), R.layout.item_calendar_day, days)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        // Highlight data atual
        val today = Calendar.getInstance()
        val currentDayOfMonth = today.get(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val currentDayIndex = firstDayOfWeek + currentDayOfMonth - 1
        if (currentDayIndex >= 0 && currentDayIndex < days.size) {
            days[currentDayIndex] = days[currentDayIndex]
        }

        // Highlight data selecionada
        selectedDate?.let {
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = it

            // Verifica mês e ano do dia selecionado se correspondem ao mês e ano atual
            val selectedMonth = calendarSelected.get(Calendar.MONTH)
            val selectedYear = calendarSelected.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            if (selectedMonth == currentMonth && selectedYear == currentYear) {
                val selectedDay = calendarSelected.get(Calendar.DAY_OF_MONTH)
                val selectedIndex = firstDayOfWeek + selectedDay

                // Highlight dia seleciondo
                if (selectedIndex >= 0 && selectedIndex < days.size) {
                    days[selectedIndex] = "(${days[selectedIndex]})"
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

    private fun getDaysInMonth(): List<String> {
        val days = mutableListOf<String>()
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        repeat(firstDayOfWeek) {
            days.add("")
        }

        repeat(lastDayOfMonth) { day ->
            val dayOfMonth = day + 1
            days.add(dayOfMonth.toString())

            if (dayOfMonth == today && cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)) {
                days[day + firstDayOfWeek] = "*$dayOfMonth*"
            }
        }

        return days
    }

    private fun handleItemClick(position: Int) {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, position - cal.get(Calendar.DAY_OF_WEEK) + 1)
        selectedDate = cal.time

        updateCalendar()
    }

}
