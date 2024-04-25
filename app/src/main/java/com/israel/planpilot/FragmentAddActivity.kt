package com.israel.planpilot

import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


class FragmentAddActivity : Fragment() {

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var descriptionCount = 0
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val selectedWeekDays = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_activity, container, false)

        val mainActivity = activity as MainActivity
        mainActivity.btnAddActivity.visibility = View.GONE

        val nameActivity = view.findViewById<EditText>(R.id.nameActivity)
        val alarmSwitch = view.findViewById<SwitchCompat>(R.id.alarmSwitch)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val timePicker = view.findViewById<Button>(R.id.timePicker)
        val alarmTone = view.findViewById<ImageButton>(R.id.alarmTone)
        val startDateButton: Button = view.findViewById(R.id.startDateButton)
        val endDateButton: Button = view.findViewById(R.id.endDateButton)

        val addDescriptionButton = view.findViewById<ImageButton>(R.id.addDescriptionButton)

        val firstDescriptionField = view.findViewById<EditText>(R.id.firstDescriptionField)
        val secondDescriptionField = view.findViewById<EditText>(R.id.secondDescriptionField)
        val thirdDescriptionField = view.findViewById<EditText>(R.id.thirdDescriptionField)

        val btnSunday = view.findViewById<Button>(R.id.btnSunday)
        val btnMonday = view.findViewById<Button>(R.id.btnMonday)
        val btnTuesday = view.findViewById<Button>(R.id.btnTuesday)
        val btnWednesday = view.findViewById<Button>(R.id.btnWednesday)
        val btnThursday = view.findViewById<Button>(R.id.btnThursday)
        val btnFriday = view.findViewById<Button>(R.id.btnFriday)
        val btnSaturday = view.findViewById<Button>(R.id.btnSaturday)

        val buttonDayMap = mapOf(
            btnSunday to "sunday",
            btnMonday to "monday",
            btnTuesday to "tuesday",
            btnWednesday to "wednesday",
            btnThursday to "thursday",
            btnFriday to "friday",
            btnSaturday to "saturday"
        )

        addDescriptionButton.setOnClickListener {
            when {
                firstDescriptionField.visibility == View.GONE -> {
                    firstDescriptionField.visibility = View.VISIBLE
                }
                secondDescriptionField.visibility == View.GONE -> {
                    secondDescriptionField.visibility = View.VISIBLE
                }
                thirdDescriptionField.visibility == View.GONE -> {
                    thirdDescriptionField.visibility = View.VISIBLE
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Você só pode adicionar até 3 descrições",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        fun updateWeekButtons(date1: LocalDate?, date2: LocalDate?) {
            buttonDayMap.keys.forEach { button ->
                if (selectedWeekDays.contains(buttonDayMap[button])) {
                    toggleWeekDaySelection(button, buttonDayMap[button]!!, selectedWeekDays)
                }
            }

            val daysBetween = ChronoUnit.DAYS.between(date1, date2)

            if (daysBetween >= 7) {
                buttonDayMap.forEach { (button, day) ->
                    toggleWeekDaySelection(button, day, selectedWeekDays)
                }
            } else {
                var currentDate = date1
                while (currentDate!!.isBefore(date2) || currentDate.isEqual(date2)) {
                    val dayOfWeek = currentDate.dayOfWeek.name.lowercase(Locale.ROOT)
                    toggleWeekDaySelection(when (dayOfWeek) {
                        "sunday" -> btnSunday
                        "monday" -> btnMonday
                        "tuesday" -> btnTuesday
                        "wednesday" -> btnWednesday
                        "thursday" -> btnThursday
                        "friday" -> btnFriday
                        "saturday" -> btnSaturday
                        else -> throw IllegalArgumentException("Invalid day of week")
                    }, dayOfWeek, selectedWeekDays)
                    currentDate = currentDate.plusDays(1)
                }
            }
        }

        val currentDate = Calendar.getInstance()
        val currentLocalDate = LocalDate.of(
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH) + 1,
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        startDate = currentLocalDate
        endDate = currentLocalDate

        println("start date: + $currentLocalDate")

        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
        val currentDateString = String.format(
            resources.getString(R.string.date_format),
            currentDay,
            currentMonth + 1,
            currentYear
        )

        selectedYear = currentYear
        selectedMonth = currentMonth + 1
        selectedDay = currentDay

        startDateButton.text = currentDateString

        val currentDayOfWeek = currentLocalDate.dayOfWeek.name.lowercase(Locale.ROOT)
        val currentButton = when (currentDayOfWeek) {
            "sunday" -> btnSunday
            "monday" -> btnMonday
            "tuesday" -> btnTuesday
            "wednesday" -> btnWednesday
            "thursday" -> btnThursday
            "friday" -> btnFriday
            "saturday" -> btnSaturday
            else -> throw IllegalArgumentException("Invalid day of week")
        }

        toggleWeekDaySelection(currentButton, currentDayOfWeek, selectedWeekDays)

        endDateButton.text = currentDateString

        startDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yearCal = if (startDate != null) {
                startDate!!.year
            } else {
                calendar.get(Calendar.YEAR)
            }

            val monthCal = if (startDate != null) {
                startDate!!.monthValue - 1
            } else {
                calendar.get(Calendar.MONTH)
            }

            val dayCal = if (startDate != null) {
                startDate!!.dayOfMonth
            } else {
                calendar.get(Calendar.DAY_OF_MONTH)
            }

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

                updateWeekButtons(startDate, endDate)

            }, yearCal, monthCal, dayCal)
            dpd.show()
        }

        endDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yearCal = if (endDate != null) {
                endDate!!.year
            } else {
                calendar.get(Calendar.YEAR)
            }

            val monthCal = if (endDate != null) {
                endDate!!.monthValue - 1
            } else {
                calendar.get(Calendar.MONTH)
            }

            val dayCal = if (endDate != null) {
                endDate!!.dayOfMonth
            } else {
                calendar.get(Calendar.DAY_OF_MONTH)
            }

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

                updateWeekButtons(startDate, endDate)

            }, yearCal, monthCal, dayCal)
            dpd.show()
        }

        btnSunday.setOnClickListener {
            toggleWeekDaySelection(btnSunday, "sunday", selectedWeekDays)
        }

        btnMonday.setOnClickListener {
            toggleWeekDaySelection(btnMonday, "monday", selectedWeekDays)
        }

        btnTuesday.setOnClickListener {
            toggleWeekDaySelection(btnTuesday, "tuesday", selectedWeekDays)
        }

        btnWednesday.setOnClickListener {
            toggleWeekDaySelection(btnWednesday, "wednesday", selectedWeekDays)
        }

        btnThursday.setOnClickListener {
            toggleWeekDaySelection(btnThursday, "thursday", selectedWeekDays)
        }

        btnSaturday.setOnClickListener {
            toggleWeekDaySelection(btnSaturday, "saturday", selectedWeekDays)
        }

        btnFriday.setOnClickListener {
            toggleWeekDaySelection(btnFriday, "friday", selectedWeekDays)
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            AddActivityUtils.setAlarmSwitchListener(isChecked)
        }

        timePicker.setOnClickListener {
            AddActivityUtils.setTimePicker(timePicker, childFragmentManager)
        }

        alarmTone.setOnClickListener {
            AddActivityUtils.setupAlarmToneButton(view, requireContext())
        }

        saveButton.setOnClickListener {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDateString = startDate?.format(formatter)
            val endDateString = endDate?.format(formatter)

            lifecycleScope.launch {
                AddActivityUtils.saveActivity(
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
        }
        return view
    }

    private fun toggleWeekDaySelection(
        button: Button,
        dayName: String,
        selectedDays: MutableList<String>
    ) {
        if (selectedDays.contains(dayName)) {
            selectedDays.remove(dayName)
            println("false")
            println("$selectedDays")
            button.isSelected = false
            button.setTextColor(Color.BLACK)
        } else {
            selectedDays.add(dayName)
            println("true")
            println("$selectedDays")
            button.isSelected = true
            button.setTextColor(Color.WHITE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as MainActivity
        mainActivity.btnAddActivity.visibility = View.VISIBLE
    }
}
