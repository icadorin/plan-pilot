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

    private lateinit var calendar: Calendar
    private lateinit var gridView: GridView
    private lateinit var selectedDate: Date
    private lateinit var textYear: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    private val activityCardRepository = ActivityCardRepository()

    private val adapter: CalendarCell by lazy {
        CalendarCell(requireContext(), R.layout.item_calendar_day, mutableListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        initViews(view)
        setupListeners()
        calendar = createCalendarInstance()
        selectedDate = createCalendarInstance().time
        gridView.adapter = adapter
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
            adapter.updateData(days)
        }
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }

    private fun setupListeners() {
        btnPrev.setOnClickListener { showPreviousMonth() }
        btnNext.setOnClickListener { showNextMonth() }
    }

    private fun showPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        lifecycleScope.launch {
            activityCardRepository.initializeCache()
            updateCalendar()
        }
    }

    private fun showNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        lifecycleScope.launch {
            activityCardRepository.initializeCache()
            updateCalendar()
        }
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

    private fun firstDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendar.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val firstDayOfWeek = (((dayOfWeek - 1) - cal.firstDayOfWeek + 7) % 7) + 1

        if (firstDayOfWeek == Constants.HORIZONTAL_CELLS) {
            return 0
        }

        return firstDayOfWeek
    }

    private fun createCalendarInstance(): Calendar {
        return Calendar.getInstance()
    }

    inner class CalendarCell(
        context: Context,
        resource: Int,
        private var dayInfos: MutableList<DayInfo>
    ) : ArrayAdapter<DayInfo>(context, resource, dayInfos) {

        fun updateData(newData: List<DayInfo>) {
            dayInfos.clear()
            dayInfos.addAll(newData)
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
