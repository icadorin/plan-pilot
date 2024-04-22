package com.israel.planpilot

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ActivityUtils {

    private var alarmToneSelected: Uri? = null
    private var alarmTimestamp: Long? = null
    private var alarmActivated: Boolean = false
    private var currentMediaPlayer: MediaPlayer? = null

    fun setTimePicker(
        timePicker: TextView,
        childFragmentManager: FragmentManager
    ) {
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
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    alarmTimestamp = calendar.timeInMillis
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            )
        timePickerDialog.show(childFragmentManager, "TimePickerDialog")
    }

    fun setupAlarmToneButton(view: View, context: Context) {
        val (list, uriList) = getRingtoneList(context)
        val adapter = createRingtoneAdapter(context, list, uriList)
        val dialogView = createDialogView(context)

        var dialog: AlertDialog? = null

        val builder = AlertDialog.Builder(context).apply {
            setCustomTitle(dialogView)
            setSingleChoiceItems(adapter, -1) { _, position ->
                selectAlarmTone(position, list, uriList, view)
                dialog?.dismiss()
            }
        }

        dialog = builder.create().apply {
            setOnDismissListener {
                currentMediaPlayer?.stop()
                currentMediaPlayer?.release()
                currentMediaPlayer = null
            }
            show()
        }
    }

    private fun setAlarm(
        activityName: String,
        alarmTone: String?,
        context: Context?,
        startDate: String?,
        endDate: String?,
        weekDays: List<String>,
        alarmTriggerTimes: String
    ) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startCalendar = Calendar.getInstance().apply {
            time = startDate?.let { date ->
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = format.parse(date)
                parsedDate
            }!!
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            time = endDate?.let { date ->
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = format.parse(date)
                parsedDate
            }!!
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarmTimesList = alarmTriggerTimes.split(",")

        while (startCalendar.before(endCalendar) || startCalendar.equals(endCalendar)) {
            val dayName = when (startCalendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "monday"
                Calendar.TUESDAY -> "tuesday"
                Calendar.WEDNESDAY -> "wednesday"
                Calendar.THURSDAY -> "thursday"
                Calendar.FRIDAY -> "friday"
                Calendar.SATURDAY -> "saturday"
                Calendar.SUNDAY -> "sunday"
                else -> ""
            }

            if (weekDays.contains(dayName)) {
                for (time in alarmTimesList) {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = startCalendar.timeInMillis
                        set(Calendar.HOUR_OF_DAY, time.split(":")[0].toInt())
                        set(Calendar.MINUTE, time.split(":")[1].toInt())
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        putExtra("alarm_id", UUID.randomUUID().toString())
                        putExtra("activity_name", activityName)
                        putExtra("alarm_time", formatter.format(calendar.time))
                        putExtra("alarm_tone", alarmTone)
                    }

                    val uniqueRequestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        uniqueRequestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(
                            context,
                            "Este aplicativo não tem as permissão para definir alarmes exatos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
            startCalendar.add(Calendar.DATE, 1)
        }
    }

    suspend fun saveActivity(
        view: View,
        nameActivity: EditText,
        timePicker: TextView,
        alarmSwitch: SwitchCompat,
        selectedDay: Int?,
        selectedMonth: Int?,
        selectedYear: Int?,
        startDate: String?,
        endDate: String?,
        selectedWeekDays: MutableList<String>?,
        scope: CoroutineScope,
        context: Context?
    ) {
        val alarmToneNameTextView = view.findViewById<TextView>(R.id.alarmToneName)
        val repository = context?.let { ActivityRepository() }
        try {
            val name = nameActivity.text.toString().trim()
            if (TextUtils.isEmpty(name)) {
                nameActivity.error = "Nome da atividade é obrigatório"
            } else if (selectedWeekDays.isNullOrEmpty()) {
                Toast.makeText(
                    context,
                    "Selecione pelo menos um dia da semana",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val alarmTriggerTime = timePicker.text.toString()
                val alarmToneString = alarmToneSelected?.toString()
                val currentTime = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date())

                val alarmActivated = ActivityUtils.alarmActivated

                val activity = Activity(
                    name = name,
                    day = selectedDay,
                    month = selectedMonth,
                    year = selectedYear,
                    time = currentTime,
                    startDate = startDate,
                    endDate = endDate,
                    contactForMessage = null,
                    alarmTriggerTime = alarmTriggerTime,
                    alarmActivated = alarmActivated,
                    alarmTone = alarmToneString,
                    category = null,
                    weekDays = selectedWeekDays
                )

                scope.launch(Dispatchers.IO) {
                    try {
                        repository?.createActivity(activity)
                        if (alarmActivated && alarmTimestamp != null) {
                            setAlarm(
                                name,
                                alarmToneString,
                                context,
                                startDate,
                                endDate,
                                selectedWeekDays,
                                alarmTriggerTime
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                view,
                                "Erro ao criar a atividade: ${e.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    nameActivity.text.clear()
                    alarmSwitch.isChecked = false
                    alarmToneSelected = null
                    "Padrão".also { alarmToneNameTextView?.text = it }

                    Snackbar.make(
                        view,
                        "Atividade criada com sucesso!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAlarmSwitchListener(isChecked: Boolean) {
        alarmActivated = isChecked
    }

    private fun getRingtoneList(context: Context): Pair<ArrayList<String>, ArrayList<Uri>> {
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = ringtoneManager.cursor
        val list = ArrayList<String>()
        val uriList = ArrayList<Uri>()

        list.add("Padrão")
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

                if (position ==  0) {
                    playButton.visibility = View.GONE
                } else {
                    playButton.visibility = View.VISIBLE
                    playButton.setImageResource(R.drawable.ic_play)

                    playButton.setOnClickListener {
                        if (currentMediaPlayer != null) {
                            currentMediaPlayer?.stop()
                            currentMediaPlayer?.release()
                            currentMediaPlayer = null
                            currentPlayButton?.setImageResource(R.drawable.ic_play)
                        }

                        if (currentPlayButton != playButton) {
                            currentMediaPlayer = MediaPlayer().apply {
                                setDataSource(context, uriList[position])
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
                                )
                                prepareAsync()
                                setOnPreparedListener {
                                    start()
                                }
                            }
                            playButton.setImageResource(R.drawable.ic_stop)
                            currentPlayButton = playButton
                        } else {
                            currentPlayButton = null
                        }
                    }
                }

                return itemView
            }
        }
    }

    private fun createDialogView(context: Context): View {
        val frameLayout = FrameLayout(context)
        return LayoutInflater.from(context).inflate(
            R.layout.list_title_ringtone,
            frameLayout,
            false
        )
    }

    private fun selectAlarmTone(position: Int, list: List<String>, uriList: List<Uri>, view: View) {
        if (position == 0) {
            alarmToneSelected = null
            val alarmToneNameTextView = view.findViewById<TextView>(R.id.alarmToneName)
            "Padrão".also { alarmToneNameTextView?.text = it }
        } else {
            alarmToneSelected = uriList[position]
            val alarmToneNameTextView = view.findViewById<TextView>(R.id.alarmToneName)
            alarmToneNameTextView?.text = list[position]
        }
    }
}
