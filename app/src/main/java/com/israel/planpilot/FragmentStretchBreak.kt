package com.israel.planpilot

import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import java.util.*
import kotlin.concurrent.timerTask

class FragmentStretchBreak : Fragment() {

    private lateinit var tvTimer: TextView
    private lateinit var btnWork: Button
    private lateinit var btnStretch: Button
    private lateinit var btnPause: Button
    private lateinit var tvCyclesCompleted: TextView
    private lateinit var tvHoursWorked: TextView
    private lateinit var tvIntervalsCompleted: TextView
    private lateinit var etWorkTime: EditText
    private lateinit var etRestTime: EditText

    private val viewModel: TimerViewModel by activityViewModels()

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stretch_break, container, false)

        tvTimer = view.findViewById(R.id.tvTimer)
        btnWork = view.findViewById(R.id.btnWork)
        btnStretch = view.findViewById(R.id.btnStretch)
        btnPause = view.findViewById(R.id.btnPause)
        tvCyclesCompleted = view.findViewById(R.id.tvCyclesCompleted)
        tvHoursWorked = view.findViewById(R.id.tvHoursWorked)
        tvIntervalsCompleted = view.findViewById(R.id.tvIntervalsCompleted)
        etWorkTime = view.findViewById(R.id.etWorkTime)
        etRestTime = view.findViewById(R.id.etRestTime)

        addTextWatcher(etWorkTime)
        addTextWatcher(etRestTime)

        for (i in 1..4) viewModel.cyclesQtdManager.add(true)

        btnWork.setOnClickListener { configureWork() }
        btnStretch.setOnClickListener { configureRest(false) }
        btnPause.setOnClickListener { togglePause() }

        updateUI()

        return view
    }

    private fun addTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                var newText = s.toString().filter { it.isDigit() }

                if (newText.length > 6) {
                    newText = newText.substring(0, 6)
                }

                val sb = StringBuilder(newText)
                if (newText.length > 2) sb.insert(2, ":")
                if (newText.length > 4) sb.insert(5, ":")

                isUpdating = true
                editText.setText(sb.toString())
                editText.setSelection(sb.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configureWork() {
        viewModel.isWorking = true
        viewModel.isResting = false
        viewModel.mainTime = if (etWorkTime.text.isNotBlank()) {
            timeToSeconds(etWorkTime.text.toString())
        } else {
            7200
        }
        startTimer()
        playSound(R.raw.bell_start)
    }

    private fun configureRest(long: Boolean) {
        viewModel.isWorking = false
        viewModel.isResting = true
        val restTime = if (etRestTime.text.isNotBlank()) {
            timeToSeconds(etRestTime.text.toString())
        } else {
            120
        }
        viewModel.mainTime = if (long) restTime * 2 else restTime
        startTimer()
        playSound(R.raw.bell_finish)
    }

    private fun timeToSeconds(time: String): Int {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        return when (parts.size) {
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            2 -> parts[0] * 60 + parts[1]
            1 -> parts[0]
            else -> 0
        }
    }

    private fun startTimer() {
        viewModel.timer?.cancel()
        viewModel.timer = Timer()
        viewModel.timer!!.schedule(timerTask {
            handler.post {
                if (viewModel.isPaused) return@post
                viewModel.mainTime--
                updateUI()

                if (viewModel.mainTime <= 0) {
                    if (viewModel.isWorking) {
                        if (viewModel.cyclesQtdManager.isNotEmpty()) {
                            viewModel.cyclesQtdManager.removeAt(0)
                            configureRest(false)
                        } else {
                            configureRest(true)
                            viewModel.completeCycles++
                            for (i in 1..4) viewModel.cyclesQtdManager.add(true)
                        }
                        viewModel.numberOfIntervals++
                    } else {
                        configureWork()
                    }
                }

                if (viewModel.isWorking) {
                    viewModel.fullWorkingTime++
                }
            }
        }, 0, 1000)
    }

    private fun togglePause() {
        viewModel.isPaused = !viewModel.isPaused
        btnPause.text = if (viewModel.isPaused) "Play" else "Pause"
    }

    private fun updateUI() {
        tvTimer.text = secondsToTime(viewModel.mainTime)
        tvCyclesCompleted.text = "Ciclos concluídos: ${viewModel.completeCycles}"
        tvHoursWorked.text = "Horas trabalhadas: ${secondsToTime(viewModel.fullWorkingTime)}"
        tvIntervalsCompleted.text = "Pomodoros concluídos: ${viewModel.numberOfIntervals}"
    }

    private fun playSound(resourceId: Int) {
        val mediaPlayer = MediaPlayer.create(requireContext(), resourceId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }

    private fun secondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        startUpdatingUI()
    }

    private fun startUpdatingUI() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!viewModel.isPaused) {
                    updateUI()
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }
}
