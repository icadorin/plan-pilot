package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.Constants.LAST_DAY_OF_WEEK
import com.israel.planpilot.Constants.VERTICAL_VISIBLE_CELLS
import java.util.Calendar
import java.util.Date

class LargeMonCalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var selectedDate: Date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        initViews(view)
        updateCalendar()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewDays)
        selectedDate = Calendar.getInstance().time

        layoutManager = GridLayoutManager(activity, LAST_DAY_OF_WEEK)
        recyclerView.layoutManager = layoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastDx = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Verifique se o usuário arrastou a tela para a direita
                if (dx != 0 && dx != lastDx) {
                    lastDx = dx
                    // O usuário arrastou a tela para a direita, então vá para o próximo mês
                    selectedDate = Calendar.getInstance().apply {
                        time = selectedDate
                        add(Calendar.MONTH, 1)
                    }.time
                    updateCalendar()
                }

                // Verifique se o usuário arrastou a tela para a esquerda
                if (dx < 0) {
                    // O usuário arrastou a tela para a esquerda, então vá para o mês anterior
                    selectedDate = Calendar.getInstance().apply {
                        time = selectedDate
                        add(Calendar.MONTH, -1)
                    }.time
                    updateCalendar()
                }
            }
        })
    }

    private fun updateCalendar() {
        val verticalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = true)
        val horizontalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = false)

        recyclerView.addItemDecoration(verticalDivider)
        recyclerView.addItemDecoration(horizontalDivider)

        recyclerView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                    val screenHeight = recyclerView.height
                    val days = getDaysInMonth()
                    val adapter = CalendarAdapter(days, screenHeight)
                    recyclerView.adapter = adapter

                    return true
                }
            }
        )
    }

    private inner class DayItem(val day: String, val isCurrentMonth: Boolean)

    private fun getDaysInMonth(): List<DayItem> {
        val days = mutableListOf<DayItem>()
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Adiciona os dias do mes anterior
        calendar.add(Calendar.MONTH, -1)
        val lastMonthMaxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysBefore = (firstDayOfWeek - 1) % LAST_DAY_OF_WEEK
        for (i in lastMonthMaxDays - daysBefore + 1..lastMonthMaxDays) {
            days.add(DayItem(i.toString(), false))
        }

        // Adiciona os dias do mes atual
        for (dayOfMonth in 1..lastDayOfMonth) {
            days.add(DayItem(dayOfMonth.toString(), true))
        }

        // Adiciona os dias do proximo mes
        calendar.add(Calendar.MONTH, 2)
        val totalCells = LAST_DAY_OF_WEEK * VERTICAL_VISIBLE_CELLS
        val daysAfter = totalCells - (days.size % totalCells)
        for (dayOfMonth in 1..daysAfter) {
            days.add(DayItem(dayOfMonth.toString(), false))
        }

        return days
    }

    private inner class CalendarAdapter(
        private val days: List<DayItem>,
        private val screenHeight: Int
    ) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_calendar_day_large,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return days.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dayItem = days[position]

            holder.textView.text = dayItem.day

            if (dayItem.isCurrentMonth) {
                holder.textView.setTextColor(ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.black)
                )
            } else {
                holder.textView.setTextColor(ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.grey)
                )
            }

            val adjustedHeight = screenHeight / VERTICAL_VISIBLE_CELLS

            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = adjustedHeight
            holder.itemView.layoutParams = layoutParams
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.textDay)
        }
    }
}

