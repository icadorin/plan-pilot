package com.israel.planpilot

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask

class FragmentStretchBreak : Fragment() {

    private lateinit var tvTimer: TextView
    private lateinit var btnWork: Button
    private lateinit var btnStretch: Button
    private lateinit var btnPause: Button
    private lateinit var tvCyclesCompleted: TextView
    private lateinit var tvHoursWorked: TextView
    private lateinit var tvIntervalsCompleted: TextView

    private var mainTime: Int = 7200
    private var isWorking = false
    private var isResting = false
    private var isPaused = false
    private var completeCycles = 0
    private var fullWorkingTime = 0
    private var numberOfIntervals = 0
    private var cyclesQtdManager = ArrayList<Boolean>()
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null

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

        for (i in 1..4) cyclesQtdManager.add(true)

        btnWork.setOnClickListener { configureWork() }
        btnStretch.setOnClickListener { configureRest(false) }
        btnPause.setOnClickListener { togglePause() }

        return view
    }

    private fun configureWork() {
        isWorking = true
        isResting = false
        mainTime = 1500
        startTimer()
        playSound(R.raw.bell_start)
    }

    private fun configureRest(long: Boolean) {
        isWorking = false
        isResting = true
        mainTime = if (long) 300 else 120
        startTimer()
        playSound(R.raw.bell_finish)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        timer!!.schedule(timerTask {
            handler.post {
                if (isPaused) return@post
                mainTime--
                updateUI()

                if (mainTime <= 0) {
                    if (isWorking) {
                        if (cyclesQtdManager.isNotEmpty()) {
                            cyclesQtdManager.removeAt(0)
                            configureRest(false)
                        } else {
                            configureRest(true)
                            completeCycles++
                            for (i in 1..4) cyclesQtdManager.add(true)
                        }
                        numberOfIntervals++
                    } else {
                        configureWork()
                    }
                }

                if (isWorking) {
                    fullWorkingTime++
                }
            }
        }, 0, 1000)
    }

    private fun togglePause() {
        isPaused = !isPaused
        btnPause.text = if (isPaused) "Play" else "Pause"
    }

    private fun updateUI() {
        tvTimer.text = secondsToTime(mainTime)
        tvCyclesCompleted.text = "Ciclos concluídos: $completeCycles"
        tvHoursWorked.text = "Horas trabalhadas: ${secondsToTime(fullWorkingTime)}"
        tvIntervalsCompleted.text = "Pomodoros concluídos: $numberOfIntervals"
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
}






