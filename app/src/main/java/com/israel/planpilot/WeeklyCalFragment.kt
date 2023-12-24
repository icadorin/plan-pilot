package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.text.SpannableString
import android.text.Spanned
import androidx.core.content.ContextCompat
import android.text.style.ForegroundColorSpan
import java.text.SimpleDateFormat
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyCalFragment : Fragment() {

    private lateinit var calendar: Calendar
    private lateinit var gridView: GridView
    private lateinit var textWeekYear: TextView
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var weekPicker: NumberPicker
    private lateinit var btnToday: Button
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weekly_cal, container, false)

        gridView = view.findViewById(R.id.gridViewDays)
        textWeekYear = view.findViewById(R.id.textYear)
        btnPrevWeek = view.findViewById(R.id.btnPrev)
        btnNextWeek = view.findViewById(R.id.btnNext)
        weekPicker = view.findViewById(R.id.weekPicker)
        btnToday = view.findViewById(R.id.btnToday)

        calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        selectedDate = today.time

        btnPrevWeek.setOnClickListener { showPreviousWeek() }
        btnNextWeek.setOnClickListener { showNextWeek() }
        btnToday.setOnClickListener {
            backToCurrentWeek()
        }

        weekPicker.minValue = 1
        weekPicker.maxValue = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
        weekPicker.value = calendar.get(Calendar.WEEK_OF_YEAR)
        weekPicker.wrapSelectorWheel = false
        weekPicker.setOnValueChangedListener { _, _, newVal -> onWeekPickerValueChanged(newVal) }

        updateCalendar()

        return view
    }

    private fun updateCalendar() {
        val weekFormat = SimpleDateFormat("w yyyy", Locale.getDefault())
        val formattedDate = weekFormat.format(calendar.time)

        val weekNumber = formattedDate.substring(0, 2)
        val year = formattedDate.substring(3)

        val monthYearFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val month = monthYearFormat.format(calendar.time)

        val firstLetter = month.substring(0, 1).uppercase(Locale.ROOT)
        val restOfMonth = month.substring(1)
        val capitalizedMonth = firstLetter + restOfMonth

        val displayText = "Sem. $weekNumber, $capitalizedMonth $year"
        textWeekYear.text = displayText

        val days = getDaysInWeek()
        val adapter = CalendarCell(requireContext(), R.layout.item_calendar_day, days)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        selectedDate?.let {
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = it

            val selectedWeek = calendarSelected.get(Calendar.WEEK_OF_YEAR)
            val selectedYear = calendarSelected.get(Calendar.YEAR)
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            if (selectedWeek == currentWeek && selectedYear == currentYear) {
                val selectedDay = calendarSelected.get(Calendar.DAY_OF_WEEK)
                val selectedIndex = calculateSelectedIndex(selectedDay)

                adapter.setSelectedDay(selectedIndex)
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun updateWeekPickerValue() {
        weekPicker.value = calendar.get(Calendar.WEEK_OF_YEAR)
    }

    private fun showPreviousWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        updateWeekPickerValue()
        updateCalendar()
    }

    private fun showNextWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        updateWeekPickerValue()
        updateCalendar()
    }

    private fun getDaysInWeek(): List<SpannableString> {
        val days = mutableListOf<SpannableString>()

        val highlightColor = context?.let {
            ContextCompat.getColor(it, R.color.green)
        } ?: Color.BLACK

        repeat(7) { dayOfWeek ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = calendar.timeInMillis
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1)

            val isToday = isToday(cal, dayOfWeek + 1)
            val spannableString = if (isToday) {
                highlightText(
                    SpannableString(cal.get(Calendar.DAY_OF_MONTH).toString()),
                    ForegroundColorSpan(highlightColor)
                )
            } else {
                SpannableString(cal.get(Calendar.DAY_OF_MONTH).toString())
            }

            days.add(spannableString)
        }

        return days
    }

    private fun handleItemClick(position: Int) {
        val selectedDayOfWeek = position + 1
        selectedDate = calculateSelectedDate(selectedDayOfWeek)
        updateCalendar()
    }

    private fun isToday(cal: Calendar, dayOfWeek: Int): Boolean {
        val today = Calendar.getInstance()
        return (
                dayOfWeek == today.get(Calendar.DAY_OF_WEEK) &&
                        cal.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                )
    }

    private fun highlightText(text: SpannableString, span: Any): SpannableString {
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return text
    }

    private fun calculateSelectedIndex(selectedDayOfWeek: Int): Int {
        return selectedDayOfWeek + 1
    }

    private fun onWeekPickerValueChanged(newVal: Int) {
        calendar.set(Calendar.WEEK_OF_YEAR, newVal)
        updateCalendar()
    }

    private fun calculateSelectedDate(selectedDayOfWeek: Int): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis
        cal.set(Calendar.DAY_OF_WEEK, selectedDayOfWeek)
        return cal.time
    }

    private fun backToCurrentWeek() {
        val today = Calendar.getInstance()
        val currentWeek = today.get(Calendar.WEEK_OF_YEAR)

        if (calendar.get(Calendar.WEEK_OF_YEAR) != currentWeek) {
            calendar.time = today.time
            updateWeekPickerValue()
            updateCalendar()
        } else {
            btnToday.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.btn_today_feedback))
        }
    }
}





