package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.israel.planpilot.Constants.HORIZONTAL_CELLS

class WeeklyCalFragment : BaseCalendarFragment() {

    private lateinit var textWeekYear: TextView
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var weekPicker: NumberPicker
    private lateinit var btnToday: Button

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        initViews(view)
        setupListeners()
        calendar = createCalendarInstance()
        val today = createCalendarInstance()
        setupWeekPicker()
        selectedDate = today.time
        updateCalendar()
        return view
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_weekly_cal
    }
    override fun initViews(view: View) {
        gridView = view.findViewById(R.id.gridViewDays)
        textWeekYear = view.findViewById(R.id.textYear)
        btnPrevWeek = view.findViewById(R.id.btnPrev)
        btnNextWeek = view.findViewById(R.id.btnNext)
        weekPicker = view.findViewById(R.id.weekPicker)
        btnToday = view.findViewById(R.id.btnToday)
    }

    override fun updateCalendar() {
        val weekFormat = getDateFormat()
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
//        val adapter = CalendarCell(requireContext(), R.layout.item_calendar_day, days)
//        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        selectedDate.let {
            val calendarSelected = Calendar.getInstance()
            calendarSelected.time = it

            if (isSameWeek(calendarSelected, calendar)) {
                val selectedDay = calendarSelected.get(Calendar.DAY_OF_WEEK)
                val selectedIndex = selectedDay + 1

//                adapter.setSelectedDay(selectedIndex)
            }
        }

//        adapter.notifyDataSetChanged()
    }

    override fun handleItemClick(position: Int) {
        val selectedDayOfWeek = position + 1
        selectedDate = calculateSelectedDate(selectedDayOfWeek)
        updateCalendar()
    }

    override fun isToday(cal: Calendar, day: Int): Boolean {
        val today = Calendar.getInstance()
        return (
            day == today.get(Calendar.DAY_OF_WEEK) &&
            cal.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) &&
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        )
    }

    override fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("w yyyy", Locale.getDefault())
    }

    override fun isSameCalendarType(calendar1: Calendar, calendar2: Calendar): Boolean {
        return isSameWeek(calendar1, calendar2)
    }

    private fun setupListeners() {
        btnPrevWeek.setOnClickListener { showPreviousWeek() }
        btnNextWeek.setOnClickListener { showNextWeek() }
        btnToday.setOnClickListener { backToCurrentWeek() }
    }

    private fun setupWeekPicker() {
        weekPicker.minValue = 1
        weekPicker.maxValue = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
        weekPicker.value = calendar.get(Calendar.WEEK_OF_YEAR)
        weekPicker.wrapSelectorWheel = false
        weekPicker.setOnValueChangedListener { _, _, newVal -> onWeekPickerValueChanged(newVal) }
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

        repeat(HORIZONTAL_CELLS) { dayOfWeek ->
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

        if (!isSameWeek(calendar, today)) {
            calendar.time = today.time
            updateWeekPickerValue()
            updateCalendar()
        } else {
            btnToday.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.btn_today_feedback
                )
            )
        }
    }
}
