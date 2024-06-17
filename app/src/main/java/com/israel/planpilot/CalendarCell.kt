package com.israel.planpilot

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

//class CalendarCell(
//    context: Context,
//    resource: Int,
//    objects: List<SpannableString>
//) : ArrayAdapter<Pair<SpannableString, Boolean>>(context, resource, objects) {
//
//    private var disabledPositions = mutableSetOf<Int>()
//    private var selectedDay: Int? = null
//
//    fun setSelectedDay(day: Int) {
//        selectedDay = day - 2
//        notifyDataSetChanged()
//    }
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
//        val textView = view.findViewById<TextView>(R.id.textDay)
//
//        val (dayText, hasActivity) = getItem(position) ?: Pair(SpannableString(""), false)
//
//        textView.text = dayText
//
//        if (disabledPositions.contains(position)) {
//            textView.visibility = View.INVISIBLE
//        } else {
//            textView.visibility = View.VISIBLE
//        }
//
//        val textColor = if (hasActivity) {
//            ContextCompat.getColor(context, R.color.green)
//        } else {
//            ContextCompat.getColor(context, R.color.red)
//        }
//
//        textView.setTextColor(textColor)
//
//        if (position == selectedDay) {
//            // Aqui você pode adicionar um destaque visual para o dia selecionado, se desejar
//            textView.setTextColor(Color.WHITE)
//        } else {
//            textView.setTextColor(textColor)
//        }
//
//        return view
//    }
//
//    override fun areAllItemsEnabled(): Boolean {
//        return false
//    }
//
//    override fun isEnabled(position: Int): Boolean {
//        return !disabledPositions.contains(position)
//    }
//}
