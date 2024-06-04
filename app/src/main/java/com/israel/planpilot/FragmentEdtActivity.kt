package com.israel.planpilot

import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class FragmentEdtActivity : Fragment() {

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var activityId: String = ""
    private val selectedWeekDays = mutableListOf<String>()
    private lateinit var activityRepository: ActivityRepository
    private lateinit var nameActivity: EditText
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var btnSunday: Button
    private lateinit var btnMonday: Button
    private lateinit var btnTuesday: Button
    private lateinit var btnWednesday: Button
    private lateinit var btnThursday: Button
    private lateinit var btnFriday: Button
    private lateinit var btnSaturday: Button
    private lateinit var timePicker: Button
    private lateinit var tone: String
    private lateinit var alarmToneName: TextView
    private lateinit var alarmSwitch: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edt_activity, container, false)

        arguments?.let { args ->
            activityId = args.getString("activityId", "")
        }

        val mainActivity = activity as MainActivity
        mainActivity.btnAddActivity.visibility = View.GONE

        activityRepository = ActivityRepository()

        nameActivity = view.findViewById(R.id.nameActivity)
        timePicker = view.findViewById(R.id.timePicker)
        startDateButton = view.findViewById(R.id.startDateButton)
        endDateButton = view.findViewById(R.id.endDateButton)
        alarmToneName = view.findViewById(R.id.alarmToneName)
        alarmSwitch = view.findViewById(R.id.alarmSwitch)
        val alarmTone = view.findViewById<ImageButton>(R.id.alarmTone)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        val addDescriptionButton = view.findViewById<ImageButton>(R.id.addDescriptionButton)

        val firstDescriptionField = view.findViewById<EditText>(R.id.firstDescriptionField)
        val secondDescriptionField = view.findViewById<EditText>(R.id.secondDescriptionField)
        val thirdDescriptionField = view.findViewById<EditText>(R.id.thirdDescriptionField)

        btnSunday = view.findViewById(R.id.btnSunday)
        btnMonday = view.findViewById(R.id.btnMonday)
        btnTuesday = view.findViewById(R.id.btnTuesday)
        btnWednesday = view.findViewById(R.id.btnWednesday)
        btnThursday = view.findViewById(R.id.btnThursday)
        btnFriday = view.findViewById(R.id.btnFriday)
        btnSaturday = view.findViewById(R.id.btnSaturday)

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

        startDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yearCal = startDate?.year ?: calendar.get(Calendar.YEAR)
            val monthCal = startDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
            val dayCal = startDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(it.context, { _, year, monthOfYear, dayOfMonth ->
                val selectedLocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                startDate = selectedLocalDate

                val dateString = DateFormatterUtils.formatLocalDateToString(selectedLocalDate)
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
            val yearCal = endDate?.year ?: calendar.get(Calendar.YEAR)
            val monthCal = endDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
            val dayCal = endDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(it.context, { _, year, monthOfYear, dayOfMonth ->
                val selectedLocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                endDate = selectedLocalDate

                val dateString = DateFormatterUtils.formatLocalDateToString(selectedLocalDate)
                endDateButton.text = dateString

                if (endDate?.isBefore(startDate) == true) {
                    startDate = endDate
                    startDateButton.text = dateString
                }

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

        btnFriday.setOnClickListener {
            toggleWeekDaySelection(btnFriday, "friday", selectedWeekDays)
        }

        btnSaturday.setOnClickListener {
            toggleWeekDaySelection(btnSaturday, "saturday", selectedWeekDays)
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            ActivityUtils.setAlarmSwitchListener(isChecked)
        }

        timePicker.setOnClickListener {
            val timePickerDialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                { _, hourOfDay, minute, _ ->
                    val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    timePicker.text = time

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    ActivityUtils.alarmTimestamp = calendar.timeInMillis

                    selectedHour = hourOfDay
                    selectedMinute = minute
                },
                selectedHour,
                selectedMinute,
                true
            )
            timePickerDialog.show(childFragmentManager, "TimePickerDialog")
        }

        alarmTone.setOnClickListener {
            ActivityUtils.setupAlarmToneButton(view, requireContext())
        }

        saveButton.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDateString = startDate?.format(formatter)
            val endDateString = endDate?.format(formatter)

            lifecycleScope.launch {
                ActivityUtils.edtActivity(
                    view,
                    activityId,
                    nameActivity,
                    timePicker,
                    alarmSwitch,
                    tone,
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
        loadActivityDetails()
        loadAlarmTone()
        return view
    }

    private fun toggleWeekDaySelection(
        button: Button,
        dayName: String,
        selectedDays: MutableList<String>
    ) {
        if (selectedDays.contains(dayName)) {
            if (selectedDays.size > 1) {
                selectedDays.remove(dayName)
                button.isSelected = false
                button.setTextColor(Color.BLACK)
            } else {
                Toast.makeText(
                    requireContext(),
                    "É necessário ter pelo menos 1 dia da semana ativo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            selectedDays.add(dayName)
            button.isSelected = true
            button.setTextColor(Color.WHITE)
        }
    }

    private fun loadActivityDetails() {
        val dateFormatPattern = "yyyy-MM-dd"
        val timeFormatPattern = "HH:mm"
        val startDateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
        val endDateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
        val timeFormatter = DateTimeFormatter.ofPattern(timeFormatPattern)

        lifecycleScope.launch {
            try {
                val activity = withContext(Dispatchers.IO) {
                    activityRepository.getActivityById(activityId)
                }
                activity?.let {
                    nameActivity.setText(it.name)

                    val startDateString = it.startDate
                    val endDateString = it.endDate
                    val timeString = it.alarmTriggerTime
                    tone = it.alarmTone.toString()
                    val alarmActivated = it.alarmActivated

                    val startDate = LocalDate.parse(startDateString, startDateFormatter)
                    val endDate = LocalDate.parse(endDateString, endDateFormatter)

                    withContext(Dispatchers.Main) {
                        startDateButton.text = DateFormatterUtils.formatLocalDateToString(startDate)
                        endDateButton.text = DateFormatterUtils.formatLocalDateToString(endDate)

                        this@FragmentEdtActivity.startDate = startDate
                        this@FragmentEdtActivity.endDate = endDate

                        val time = LocalTime.parse(timeString, timeFormatter)
                        val originalHour = time.hour
                        val originalMinute = time.minute

                        val formattedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            originalHour,
                            originalMinute
                        )

                        timePicker.text = formattedTime
                        selectedHour = originalHour
                        selectedMinute = originalMinute
                        alarmSwitch.isChecked = alarmActivated
                    }

                    withContext(Dispatchers.Main) {
                        selectedWeekDays.clear()
                        it.weekDays?.let { it1 -> selectedWeekDays.addAll(it1) }

                        val buttonDayMap = mapOf(
                            btnSunday to "sunday",
                            btnMonday to "monday",
                            btnTuesday to "tuesday",
                            btnWednesday to "wednesday",
                            btnThursday to "thursday",
                            btnFriday to "friday",
                            btnSaturday to "saturday"
                        )

                        buttonDayMap.forEach { (button, day) ->
                            if (selectedWeekDays.contains(day)) {
                                button.isSelected = true
                                button.setTextColor(Color.WHITE)
                            } else {
                                button.isSelected = false
                                button.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Erro ao carregar detalhes da atividade: ${e.message}")
            }
        }
    }

    private fun loadAlarmTone() {
        lifecycleScope.launch {
            try {
                val activity = withContext(Dispatchers.IO) {
                    activityRepository.getActivityById(activityId)
                }
                activity?.let {
                    val alarmToneUri = it.alarmTone
                    alarmToneUri?.let { uri ->
                        val alarmTone = RingtoneManager.getRingtone(requireContext(), Uri.parse(uri))
                        val alarmToneTitle = alarmTone.getTitle(requireContext())
                        alarmToneName.text = alarmToneTitle ?: "Padrão"
                    }
                }
            } catch (e: Exception) {
                println("Erro ao carregar alarme: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as MainActivity
        mainActivity.btnAddActivity.visibility = View.VISIBLE
    }
}
