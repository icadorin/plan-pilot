package com.israel.planpilot

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

abstract class BaseCalendarFragment : Fragment() {

    protected lateinit var calendar: Calendar
    protected lateinit var gridView: GridView
    protected lateinit var selectedDate: Date

    protected abstract fun getLayoutId(): Int
    protected abstract fun initViews(view: View)
    protected abstract fun updateCalendar()
    protected abstract fun handleItemClick(position: Int)
    protected abstract fun getDateFormat(): SimpleDateFormat
    protected abstract fun isToday(cal: Calendar, day: Int): Boolean
    protected abstract fun isSameCalendarType(calendar1: Calendar, calendar2: Calendar): Boolean

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        calendar = Calendar.getInstance()
        selectedDate = Calendar.getInstance().time

        return view
    }

    protected fun createCalendarInstance(): Calendar {
        return Calendar.getInstance()
    }

    protected open fun highlightText(text: SpannableString, span: Any): SpannableString {
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return text
    }

    protected fun isSameMonth(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }

    protected fun isSameWeek(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.WEEK_OF_YEAR) == date2.get(Calendar.WEEK_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }
}
