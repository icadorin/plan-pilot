package com.israel.planpilot

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.israel.planpilot.Constants.LAST_DAY_OF_WEEK
import java.util.Calendar
import java.util.Date

class LargeMonCalFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var selectedDate: Date

    private inner class DayItem(val day: String, val isCurrentMonth: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        initViews(view)
        return view
    }

//    override fun onResume() {
//        super.onResume()
//        viewPager.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
//    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        selectedDate = Calendar.getInstance().time

        val adapter = CalendarPagerAdapter()
        viewPager.adapter = adapter

        viewPager.adapter = CalendarPagerAdapter()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

        })
    }

    private inner class CalendarPagerAdapter : RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_page, parent, false)
            return CalendarViewHolder(view)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            val currentMonthDays = getDaysInYear()[position]
            holder.setDays(currentMonthDays)
        }

        override fun getItemCount(): Int {
            // Você pode ajustar a quantidade de páginas aqui, por exemplo, retornando 12 para um ano inteiro
            return 12
        }

        private inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewDays)
            private val adapter = CalendarAdapter()

            init {
                recyclerView.layoutManager = GridLayoutManager(context, LAST_DAY_OF_WEEK)
                recyclerView.adapter = adapter
            }

            fun setDays(newDays: List<DayItem>) {
                adapter.setDays(newDays)
            }

        }
    }

    private fun getDaysInYear(): List<List<DayItem>> {
        val daysInYear = mutableListOf<List<DayItem>>()

        for (month in 0 until 12) {
            val daysInMonth = mutableListOf<DayItem>()
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            calendar.add(Calendar.DATE, -dayOfWeek)

            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (dayOfMonth in 1..42) { // Ajuste para garantir 6

                var firstDay = dayOfMonth

                if (firstDay != calendar.get(Calendar.DAY_OF_MONTH)) {
                    firstDay = 1
                }

                val isCurrentMonth = firstDay <= lastDayOfMonth && month == calendar.get(Calendar.MONTH)

                daysInMonth.add(
                    DayItem(
                        (calendar.get(Calendar.DAY_OF_MONTH)).toString(),
                        isCurrentMonth
                    )
                )
                calendar.add(Calendar.DATE, 1)
            }

            daysInYear.add(daysInMonth)
        }

        return daysInYear
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

            // Adicionando o ouvinte de clique
            holder.textView.setOnClickListener {
                // Ação que você deseja realizar ao clicar no número
                Toast.makeText(
                    holder.itemView.context,
                    "Número clicado: ${dayItem.day}",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun addDecoratorsToRecyclerView(viewPager: ViewPager2) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(viewPager) as RecyclerView

        val verticalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = true)
        val horizontalDivider = GridDividerDecoration(Color.BLACK, 1f, isVertical = false)

        recyclerView.addItemDecoration(verticalDivider)
        recyclerView.addItemDecoration(horizontalDivider)
    }

}
