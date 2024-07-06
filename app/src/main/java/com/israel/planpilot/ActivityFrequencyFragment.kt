package com.israel.planpilot

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityFrequencyFragment : Fragment() {

    private lateinit var textYear: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var gridView: GridView
    private lateinit var selectedDate: Date
    private lateinit var calendar: Calendar

    private val activityCardRepository = ActivityCardRepository()

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

    private fun getLayoutId(): Int {
        return R.layout.fragment_small_mon_cal
    }

    private fun initViews(view: View) {
        gridView = view.findViewById(R.id.gridViewDays)
        textYear = view.findViewById(R.id.textYear)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
    }

    private fun updateCalendar() {
        val monthFormat = getDateFormat()
        textYear.text = monthFormat.format(calendar.time)

        lifecycleScope.launch {
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
    }

    private fun handleItemClick(position: Int) {
        val firstDayOfWeek = firstDayOfWeek()
        val selectedDayOfMonth = position - firstDayOfWeek + 1
        selectedDate = calculateSelectedDate(selectedDayOfMonth)
        updateCalendar()
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault())
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

    data class DayInfo(val dayOfMonth: Int, val drawable: Drawable?)

    private suspend fun getDaysInMonth(): List<DayInfo> {
        val days = mutableListOf<DayInfo>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis

        val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfWeek()

        repeat(firstDayOfWeek) {
            days.add(DayInfo(0, null))
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val activityCards = activityCardRepository.getAllActivityCards()

        repeat(lastDayOfMonth) { day ->
            val dayOfMonth = day + 1
            val calDay = Calendar.getInstance()
            calDay.timeInMillis = calendar.timeInMillis
            calDay.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val completedCards = activityCards.filter { card ->
                val cardDate = card.date?.let { dateFormat.parse(it) }?.let {
                    Calendar.getInstance().apply { time = it }
                }

                cardDate?.let {
                    calDay.get(Calendar.DAY_OF_MONTH) == it.get(Calendar.DAY_OF_MONTH) &&
                            calDay.get(Calendar.MONTH) == it.get(Calendar.MONTH) &&
                            calDay.get(Calendar.YEAR) == it.get(Calendar.YEAR)
                } ?: false
            }

            val isCompleted = completedCards.any { it.completed == true }
            val isNotCompleted = completedCards.any { it.completed == false }

            val drawableDefault: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.highlight_default)
            val drawableCompleted: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.highlight_color_completed)
            val drawableNotCompleted: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.highlight_color_not_completed)

            var drawable: Drawable = drawableDefault
                ?: throw IllegalStateException("Drawable cannot be null")

            if (isCompleted) {
                drawable = drawableCompleted
                    ?: throw IllegalStateException("Drawable cannot be null")
            } else if (isNotCompleted) {
                drawable = drawableNotCompleted
                    ?: throw IllegalStateException("Drawable cannot be null")
            }

            days.add(DayInfo(dayOfMonth, drawable))
        }

        return days
    }

    private fun isSameMonth(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }

    private fun firstDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val firstDayOfWeek = (((dayOfWeek - 1) - cal.firstDayOfWeek + 7) % 7) + 1

        // Caso o dia 1 seja uma segunda-feira, reseta o índice
        if (firstDayOfWeek == Constants.HORIZONTAL_CELLS) {
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

    private fun createCalendarInstance(): Calendar {
        return Calendar.getInstance()
    }

    open class CalendarCell(
        context: Context,
        resource: Int,
        objects: List<DayInfo>
    ) : ArrayAdapter<DayInfo>(context, resource, objects) {

        private var selectedDay: Int? = null

        fun setSelectedDay(day: Int?) {
            selectedDay = day
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
            val textView = view.findViewById<TextView>(R.id.textDay)

            val dayInfo = getItem(position)
            if (dayInfo != null) {
                if (dayInfo.dayOfMonth != 0) {
                    textView.text = dayInfo.dayOfMonth.toString()
                    textView.background = dayInfo.drawable
                } else {
                    textView.text = ""
                    textView.background = null
                }
            }
            return view
        }
    }
}
