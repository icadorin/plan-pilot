package com.israel.planpilot

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddActivityDialog : DialogFragment() {

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val selectedWeekDays = mutableListOf<String>()

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
            val startDateButton: Button = view.findViewById(R.id.startDateButton)
            val endDateButton: Button = view.findViewById(R.id.endDateButton)

            val currentDate = Calendar.getInstance()
            val currentLocalDate = LocalDate.of(
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH) + 1,
                currentDate.get(Calendar.DAY_OF_MONTH)
            )
            startDate = currentLocalDate
            endDate = currentLocalDate

            val currentYear = currentDate.get(Calendar.YEAR)
            val currentMonth = currentDate.get(Calendar.MONTH)
            val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
            val currentDateString = String.format(
                resources.getString(R.string.date_format),
                currentDay,
                currentMonth,
                currentYear
            )

            selectedYear = currentYear
            selectedMonth = currentMonth + 1
            selectedDay = currentDay

            startDateButton.text = currentDateString
            endDateButton.text = currentDateString

            startDateButton.setOnClickListener {
                val calendar = Calendar.getInstance()
                val yearCal = if (startDate != null) startDate!!.year else calendar.get(Calendar.YEAR)
                val monthCal = if (startDate != null) startDate!!.monthValue - 1 else calendar.get(Calendar.MONTH)
                val dayCal = if (startDate != null) startDate!!.dayOfMonth else calendar.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(it.context, { _, year, monthOfYear, dayOfMonth ->
                    val selectedLocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    startDate = selectedLocalDate

                    val dateString = String.format(
                        resources.getString(R.string.date_format),
                        dayOfMonth,
                        monthOfYear + 1,
                        year
                    )
                    startDateButton.text = dateString

                    if (startDate?.isAfter(endDate) == true) {
                        endDate = startDate
                        endDateButton.text = dateString
                    }

                }, yearCal, monthCal, dayCal)

                dpd.show()
            }

            endDateButton.setOnClickListener {
                val calendar = Calendar.getInstance()
                val yearCal = if (endDate != null) endDate!!.year else calendar.get(Calendar.YEAR)
                val monthCal = if (endDate != null) endDate!!.monthValue - 1 else calendar.get(Calendar.MONTH)
                val dayCal = if (endDate != null) endDate!!.dayOfMonth else calendar.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(it.context, { _, year, monthOfYear, dayOfMonth ->
                    val selectedLocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    endDate = selectedLocalDate

                    val dateString = String.format(
                        resources.getString(R.string.date_format),
                        dayOfMonth,
                        monthOfYear + 1,
                        year
                    )

                    endDateButton.text = dateString

                    if (endDate?.isBefore(startDate) == true) {
                        startDate = endDate
                        startDateButton.text = dateString
                    }

                }, yearCal, monthCal, dayCal)

                dpd.show()
            }

            val btnSunday = view.findViewById<Button>(R.id.btnSunday)
            btnSunday.setOnClickListener {
                toggleWeekDaySelection(btnSunday, "Sunday", selectedWeekDays)
            }

            val btnMonday = view.findViewById<Button>(R.id.btnMonday)
            btnMonday.setOnClickListener {
                toggleWeekDaySelection(btnMonday, "Monday", selectedWeekDays)
            }

            val btnTuesday = view.findViewById<Button>(R.id.btnTuesday)
            btnTuesday.setOnClickListener {
                toggleWeekDaySelection(btnTuesday, "Tuesday", selectedWeekDays)
            }

            val btnWednesday = view.findViewById<Button>(R.id.btnWednesday)
            btnWednesday.setOnClickListener {
                toggleWeekDaySelection(btnWednesday, "Wednesday", selectedWeekDays)
            }

            val btnThursday = view.findViewById<Button>(R.id.btnThursday)
            btnThursday.setOnClickListener {
                toggleWeekDaySelection(btnThursday, "Thursday", selectedWeekDays)
            }

            val btnSaturday = view.findViewById<Button>(R.id.btnSaturday)
            btnSaturday.setOnClickListener {
                toggleWeekDaySelection(btnSaturday, "Saturday", selectedWeekDays)
            }

            val btnFriday = view.findViewById<Button>(R.id.btnFriday)
            btnFriday.setOnClickListener {
                toggleWeekDaySelection(btnFriday, "Friday", selectedWeekDays)
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

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val startDateString = startDate?.format(formatter)
                val endDateString = endDate?.format(formatter)

                ActivityUtils.saveActivity(
                    view,
                    nameActivity,
                    timePicker,
                    alarmSwitch,
                    selectedDay,
                    selectedMonth,
                    selectedYear,
                    startDateString,
                    endDateString,
                    selectedWeekDays,
                    lifecycleScope,
                    context
                )
            }

            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Atividade nula")
    }

    private fun toggleWeekDaySelection(
        button: Button,
        dayName: String,
        selectedDays: MutableList<String>
    ) {
        if (selectedDays.contains(dayName)) {
            selectedWeekDays.remove(dayName)
            button.isSelected = false
            button.setTextColor(Color.BLACK)
        } else {
            selectedWeekDays.add(dayName)
            button.isSelected = true
            button.setTextColor(Color.WHITE)
        }
    }
}
