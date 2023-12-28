package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.israel.planpilot.Constants.LAST_DAY_OF_WEEK

class SmallMonCalFragment : BaseCalendarFragment() {

    private lateinit var textYear: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        initViews(view)
        setupListeners()
        calendar = createCalendarInstance()
        selectedDate = createCalendarInstance().time
        updateCalendar()
        return view
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_small_mon_cal
    }

    override fun initViews(view: View) {
        gridView = view.findViewById(R.id.gridViewDays)
        textYear = view.findViewById(R.id.textYear)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
    }

    override fun updateCalendar() {
        val monthFormat = getDateFormat()
        textYear.text = monthFormat.format(calendar.time)

        val days = getDaysInMonth()
        val adapter = CalendarCell(requireContext(), R.layout.item_calendar_day, days)

        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        val firstDayOfWeek = firstDayOfWeek()

        selectedDate.let {
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = it

            if (isSameMonth(calendarSelected, calendar)) {
                val selectedDay = calendarSelected.get(Calendar.DAY_OF_MONTH)

                if (selectedDay <= days.size) {
                    val selectedIndex = calculateSelectedIndex(it, firstDayOfWeek)
                    adapter.setSelectedDay(selectedIndex)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    override fun handleItemClick(position: Int) {
        val firstDayOfWeek = firstDayOfWeek()
        val selectedDayOfMonth = position - firstDayOfWeek + 1
        selectedDate = calculateSelectedDate(selectedDayOfMonth)
        updateCalendar()
    }

    override fun isToday(cal: Calendar, day: Int): Boolean {
        val today = Calendar.getInstance()
        return (
            day == today.get(Calendar.DAY_OF_MONTH) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        )
    }

    override fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }

    override fun isSameCalendarType(calendar1: Calendar, calendar2: Calendar): Boolean {
        return isSameMonth(calendar1, calendar2)
    }

    private fun setupListeners() {
        btnPrev.setOnClickListener { showPreviousMonth() }
        btnNext.setOnClickListener { showNextMonth() }
    }

    private fun getMonthAndYear(calendar: Calendar): Pair<Int, Int> {
        return Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
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
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis

        val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfWeek()

        repeat(firstDayOfWeek) {
            days.add(SpannableString(""))
        }

        val isSixthCellEmpty = days.getOrNull(6) == SpannableString("")

        if (isSixthCellEmpty) {
            days.clear()
        }

        val highlightColor = context?.let {
            ContextCompat.getColor(it, R.color.green)
        } ?: Color.BLACK

        repeat(lastDayOfMonth) { day ->
            val dayOfMonth = day + 1
            val isToday = isToday(cal, dayOfMonth)
            val spannableString = if (isToday) {
                highlightText(
                    SpannableString(dayOfMonth.toString()),
                    ForegroundColorSpan(highlightColor)
                )
            } else {
                SpannableString(dayOfMonth.toString())
            }

            days.add(spannableString)
        }

        return days
    }

    private fun firstDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val firstDayOfWeek = (((dayOfWeek - 1) - cal.firstDayOfWeek + 7) % 7) + 1

        // Caso o dia 1 seja uma segunda-feira, reseta o índice
        if (firstDayOfWeek == LAST_DAY_OF_WEEK) {
            return 0
        }

        return firstDayOfWeek
    }

    private fun calculateSelectedDate(selectedDay: Int): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, selectedDay)
        return cal.time
    }

    private fun calculateSelectedIndex(selectedDate: Date, firstDayOfWeek: Int): Int {
        val calendarSelected = Calendar.getInstance()
        calendarSelected.time = selectedDate

        val (selectedMonth, selectedYear) = getMonthAndYear(calendarSelected)
        val (currentMonth, currentYear) = getMonthAndYear(calendar)

        if (selectedMonth == currentMonth && selectedYear == currentYear) {
            val selectedDay = calendarSelected.get(Calendar.DAY_OF_MONTH)
            return selectedDay + firstDayOfWeek + 1
        }

        return -1
    }
}
