package com.israel.planpilot

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope

class AddActivityDialog : DialogFragment() {

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_activity, null, false)

            val nameActivity = view.findViewById<EditText>(R.id.nameActivity)
            val alarmSwitch = view.findViewById<SwitchCompat>(R.id.alarmSwitch)
            val saveButton = view.findViewById<Button>(R.id.saveButton)
            val timePicker = view.findViewById<Button>(R.id.timePicker)
            val alarmTone = view.findViewById<ImageButton>(R.id.alarmTone)
            val dateButton: Button = view.findViewById(R.id.dateButton)

            val currentDate = Calendar.getInstance()
            val currentYear = currentDate.get(Calendar.YEAR)
            val currentMonth = currentDate.get(Calendar.MONTH)
            val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
            val currentDateString = String.format(
                resources.getString(R.string.date_format),
                currentDay,
                currentMonth + 1,
                currentYear
            )
            dateButton.text = currentDateString

            dateButton.setOnClickListener {
                val calendar = Calendar.getInstance()
                val yearCal = calendar.get(Calendar.YEAR)
                val monthCal = calendar.get(Calendar.MONTH)
                val dayCal = calendar.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(it.context, { _, year, monthOfYear, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = monthOfYear + 1
                    selectedDay = dayOfMonth

                    val dateString = String.format(
                        resources.getString(R.string.date_format),
                        dayOfMonth,
                        monthOfYear + 1,
                        year
                    )
                    dateButton.text = dateString
                }, yearCal, monthCal, dayCal)

                dpd.show()
            }

            alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                ActivityUtils.setAlarmSwitchListener(isChecked)
            }

            timePicker.setOnClickListener {
                ActivityUtils.setTimePicker(timePicker, childFragmentManager)
            }

            alarmTone.setOnClickListener {
                ActivityUtils.setupAlarmToneButton(view, requireContext())
            }

            saveButton.setOnClickListener {
                ActivityUtils.saveActivity(
                    view,
                    nameActivity,
                    timePicker,
                    alarmSwitch,
                    selectedDay,
                    selectedMonth,
                    selectedYear,
                    lifecycleScope,
                    context
                )
            }

            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Atividade nula")
    }
}
