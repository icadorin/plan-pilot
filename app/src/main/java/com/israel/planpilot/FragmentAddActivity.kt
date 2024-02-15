package com.israel.planpilot

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class FragmentAddActivity : Fragment() {

    private var alarmTimestamp: Long? = null
    private var alarmToneSelected: Uri? = null
    private var currentRingtone: Ringtone? = null
    private var activityId: UUID? = null
    private var dialog: AlertDialog? = null

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"
        private const val ARG_SELECTED_MONTH = "selected_month"
        private const val ARG_SELECTED_YEAR = "selected_year"

        fun newInstance(
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): FragmentAddActivity {
            val fragment = FragmentAddActivity()
            val args = Bundle()
            args.putInt(ARG_SELECTED_DAY, selectedDay)
            args.putInt(ARG_SELECTED_MONTH, selectedMonth)
            args.putInt(ARG_SELECTED_YEAR, selectedYear)
            fragment.arguments = args

            return fragment
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.
    RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            alarmTimestamp?.let { timestamp ->
                activityId?.let { id ->
                    setAlarm(timestamp, id)
                }
            }
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SET_ALARM)) {
                showExplanationAndAskAgain()
            }
        }
    }

    private fun showExplanationAndAskAgain() {
        requestPermissionLauncher.launch(Manifest.permission.SET_ALARM)
    }

    private fun setAlarm(alarmTimestamp: Long, activityId: UUID) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_TONE", alarmToneSelected.toString())
        }
        val uniqueRequestCode = activityId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Este aplicativo não tem as permissão para definir alarmes exatos.",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimestamp, pendingIntent)
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Erro ao definir o alarme: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_activity, container, false)
    }

    private fun hideKeyboard(view: View?) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        view?.clearFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)
        val selectedMonth = arguments?.getInt(ARG_SELECTED_MONTH)
        val selectedYear = arguments?.getInt(ARG_SELECTED_YEAR)

        val monthNames = arrayOf(
            "JAN", "FEV", "MAR", "ABR",
            "MAI", "JUN", "JUL", "AGO",
            "SET", "OUT", "NOV", "DEZ"
        )

        val monthName = "${monthNames[selectedMonth!! - 1]}."

        view.findViewById<TextView>(R.id.selectedMonth).text = monthName
        view.findViewById<TextView>(R.id.selectedYear).text = selectedYear.toString()
        view.findViewById<TextView>(R.id.selectedDay).text = selectedDay.toString()

        val nameActivity = view.findViewById<EditText>(R.id.nameActivity)
        val backspaceButton = view.findViewById<Button>(R.id.backspaceButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val triggerTime = view.findViewById<Button>(R.id.triggerTime)
        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
        val alarmSwitch = view.findViewById<SwitchCompat>(R.id.alarmSwitch)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val timePicker = view.findViewById<Button>(R.id.timePicker)
        val alarmTone = view.findViewById<ImageButton>(R.id.alarmTone)
        var alarmActivated = false

        nameActivity.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                nameActivity.gravity = Gravity.START
                nameActivity.hint = ""
                backspaceButton.visibility = View.VISIBLE
                cancelButton.visibility = View.VISIBLE
                nameActivity.setSelection(nameActivity.text.length)
            } else {
                nameActivity.gravity = Gravity.CENTER
                nameActivity.hint = getString(R.string.activity_name)
                backspaceButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
            }
        }

        backspaceButton.setOnClickListener {
            nameActivity.text.clear()
        }

        cancelButton.setOnClickListener {
            nameActivity.text.clear()
            hideKeyboard(nameActivity)
        }

        nameActivity.imeOptions = EditorInfo.IME_ACTION_DONE
        nameActivity.setRawInputType(InputType.TYPE_CLASS_TEXT)

        nameActivity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(nameActivity)
                true
            } else {
                false
            }
        }

        triggerTime.setOnClickListener {
            timePicker.visibility = View.VISIBLE
            alarmSwitch.visibility = View.VISIBLE
            closeButton.visibility = View.VISIBLE
            alarmTone.visibility = View.VISIBLE
            triggerTime.visibility = View.GONE
        }

        closeButton.setOnClickListener {
            timePicker.visibility = View.GONE
            alarmSwitch.visibility = View.GONE
            closeButton.visibility = View.GONE
            alarmTone.visibility = View.GONE
            triggerTime.visibility = View.VISIBLE
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmActivated = isChecked
        }

        timePicker.setOnClickListener {
            val now = Calendar.getInstance()
            val timePickerDialog = com.wdullaer.materialdatetimepicker.time
                .TimePickerDialog
                .newInstance(
                    { _, hourOfDay, minute, _ ->
                        val time = String.format("%02d:%02d", hourOfDay, minute)
                        timePicker.text = time

                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND,  0)
                            set(Calendar.MILLISECOND,  0)
                        }
                        alarmTimestamp = calendar.timeInMillis
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                )

            timePickerDialog.accentColor = ContextCompat.getColor(
                requireContext(),
                R.color.midnight_purple
            )

            timePickerDialog.show(childFragmentManager, "TimePickerDialog")
        }

        alarmTone.setOnClickListener {
            context?.let { nonNullContext ->
                val (list, uriList) = getRingtoneList(nonNullContext)

                val builder = AlertDialog.Builder(nonNullContext)
                val adapter = createRingtoneAdapter(nonNullContext, list, uriList)

                builder.setAdapter(adapter) { _, position ->
                    handleRingtoneSelection(position, list, uriList)
                }

                context?.let {
                    val frameLayout = FrameLayout(it)
                    val dialogView = LayoutInflater.from(it).inflate(
                        R.layout.list_title_ringtone,
                        frameLayout,
                        false
                    )
                    builder.setCustomTitle(dialogView)
                }

                dialog = builder.create()

                dialog?.setOnDismissListener {
                    currentRingtone?.stop()
                }

                dialog?.show()
            }
        }

        saveButton.setOnClickListener {

            val repository = ActivityRepository(requireContext())
            try {
                val name = nameActivity.text.toString().trim()
                if (TextUtils.isEmpty(name)) {
                    nameActivity.error = "Nome da atividade é obrigatório"
                } else {
                    val time = timePicker.text.toString()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val localTime = LocalTime.parse(time, formatter)

                    val activity = Activity(
                        name = name,
                        day = selectedDay,
                        month = selectedMonth,
                        year = selectedYear,
                        contactForMessage = null,
                        alarmTriggerTime = localTime,
                        alarmActivated = alarmActivated,
                        alarmTone = alarmToneSelected,
                        category = null
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.createActivity(activity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun handleRingtoneSelection(position: Int, list: List<String>, uriList: List<Uri>) {
        if (position == 0) {
            alarmToneSelected = null
            val alarmToneNameTextView = view?.findViewById<TextView>(R.id.alarmToneName)
            alarmToneNameTextView?.text = ""
        } else {
            alarmToneSelected = uriList[position]
            val alarmToneNameTextView = view?.findViewById<TextView>(R.id.alarmToneName)
            alarmToneNameTextView?.text = list[position]
        }
        dialog?.dismiss()
    }

    private fun getRingtoneList(context: Context): Pair<ArrayList<String>, ArrayList<Uri>> {
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = ringtoneManager.cursor
        val list = ArrayList<String>()
        val uriList = ArrayList<Uri>()

        list.add("Nenhum")
        uriList.add(Uri.EMPTY)

        while (cursor.moveToNext()) {
            val name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            list.add(name)
            uriList.add(uri)
        }

        return Pair(list, uriList)
    }

    private fun createRingtoneAdapter(
        context: Context,
        list: List<String>,
        uriList: List<Uri>
    ): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(
            context,
            R.layout.list_item_ringtone,
            list
        ) {
            var currentPlayButton: ImageView? = null

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val itemView = convertView ?: LayoutInflater.from(context)
                    .inflate(
                        R.layout.list_item_ringtone,
                        parent,
                        false
                    )
                val ringtoneName = itemView.findViewById<TextView>(R.id.ringtone_name)
                val playButton = itemView.findViewById<ImageView>(R.id.play_button)

                ringtoneName.text = list[position]

                var isPlaying = false

                playButton.setOnClickListener {
                    if (isPlaying) {
                        currentRingtone?.stop()
                        playButton.setImageResource(R.drawable.ic_play)
                        isPlaying = false
                    } else {
                        currentRingtone?.stop()
                        currentPlayButton?.setImageResource(R.drawable.ic_play)

                        currentRingtone = RingtoneManager.getRingtone(
                            context,
                            uriList[position]
                        ).apply {
                            play()
                        }
                        playButton.setImageResource(R.drawable.ic_stop)
                        isPlaying = true
                        currentPlayButton = playButton
                    }
                }

                itemView.setOnClickListener {
                    if (position ==  0) {
                        alarmToneSelected = null
                        val alarmToneNameTextView = view?.findViewById<TextView>(R.id.alarmToneName)
                        alarmToneNameTextView?.text = ""
                    } else {
                        alarmToneSelected = uriList[position]
                        val alarmToneNameTextView = view?.findViewById<TextView>(R.id.alarmToneName)
                        alarmToneNameTextView?.text = list[position]
                    }
                    dialog?.dismiss()
                }

                return itemView
            }
        }
    }
}
