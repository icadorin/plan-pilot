package com.israel.planpilot

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.israel.planpilot.model.TimerViewModel
import com.israel.planpilot.utils.Constants
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask

class StretchBreakFragment : Fragment() {

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

        val inputMethodManager = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        tvTimer = view.findViewById(R.id.tvTimer)
        btnWork = view.findViewById(R.id.btnWork)
        btnStretch = view.findViewById(R.id.btnStretch)
        btnPause = view.findViewById(R.id.btnPause)
        tvCyclesCompleted = view.findViewById(R.id.tvCyclesCompleted)
        tvHoursWorked = view.findViewById(R.id.tvHoursWorked)
        tvIntervalsCompleted = view.findViewById(R.id.tvIntervalsCompleted)
        etWorkTime = view.findViewById(R.id.etWorkTime)
        etRestTime = view.findViewById(R.id.etRestTime)

        loadPreferences()

        addTextWatcher(etWorkTime)
        addTextWatcher(etRestTime)

        for (i in Constants.INITIAL_CYCLES_COUNT..Constants.MAX_CYCLES_COUNT) {
            viewModel.cyclesQtdManager.add(true)
        }

        btnWork.setOnClickListener { configureWork() }
        btnStretch.setOnClickListener { configureRest(false) }
        btnPause.setOnClickListener { togglePause() }

        updateUI()

        etWorkTime.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etWorkTime.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(etWorkTime.windowToken, 0)
                savePreferences()
                true
            } else {
                false
            }
        }

        etRestTime.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etRestTime.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(etRestTime.windowToken, 0)
                savePreferences()
                true
            } else {
                false
            }
        }

        return view
    }

    private fun addTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                var newText = s.toString().filter { it.isDigit() }

                if (newText.length > Constants.MAX_ALLOWED_LENGTH) {
                    newText = newText.substring(0, Constants.MAX_ALLOWED_LENGTH)
                }

                val sb = StringBuilder(newText)

                if (newText.length > Constants.FIRST_COLON_INSERT_POSITION) {
                    sb.insert(Constants.FIRST_COLON_INSERT_POSITION, ":")
                }
                if (newText.length > Constants.SECOND_COLON_INSERT_POSITION) {
                    sb.insert(Constants.SECOND_COLON_INSERT_POSITION, ":")
                }

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
            Constants.DEFAULT_WORK_TIME_SECONDS
        }
        startTimer()
    }

    private fun configureRest(long: Boolean) {
        viewModel.isWorking = false
        viewModel.isResting = true
        val restTime = if (etRestTime.text.isNotBlank()) {
            timeToSeconds(etRestTime.text.toString())
        } else {
            Constants.DEFAULT_REST_TIME_SECONDS
        }
        viewModel.mainTime = if (long) restTime * 2 else restTime
        startTimer()
    }

    private fun timeToSeconds(time: String): Int {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        return when (parts.size) {
            3 -> parts[0] * Constants.SECONDS_IN_HOUR +
                    parts[1] * Constants.SECONDS_IN_MINUTE +
                    parts[2]
            2 -> parts[0] * Constants.SECONDS_IN_MINUTE + parts[1]
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
                            playSound(R.raw.bell_start)
                        } else {
                            configureRest(true)
                            viewModel.completeCycles++

                            for (i in Constants.INITIAL_CYCLES_COUNT..Constants.MAX_CYCLES_COUNT) {
                                viewModel.cyclesQtdManager.add(true)
                            }

                            playSound(R.raw.bell_finish)
                        }
                        viewModel.numberOfIntervals++
                    } else {
                        configureWork()
                        playSound(R.raw.bell_finish)
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

        if (viewModel.isWorking) {
            tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_default))
        } else if (viewModel.isResting) {
            tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        }

        tvTimer.text = secondsToTime(viewModel.mainTime)
        val completedCyclesText = resources.getString(
            R.string.completed_cycles,
            viewModel.completeCycles
        )

        val hoursWorkedText = resources.getString(
            R.string.hours_worked,
            secondsToTime(viewModel.fullWorkingTime)
        )

        val intervalsCompletedText = resources.getString(
            R.string.intervals_completed,
            viewModel.numberOfIntervals
        )

        tvCyclesCompleted.text = completedCyclesText
        tvHoursWorked.text = hoursWorkedText
        tvIntervalsCompleted.text = intervalsCompletedText
    }

    private fun playSound(resourceId: Int) {
        val mediaPlayer = MediaPlayer.create(requireContext(), resourceId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }

    private fun secondsToTime(seconds: Int): String {
        val hours = seconds / Constants.SECONDS_IN_HOUR
        val minutes = (seconds % Constants.SECONDS_IN_HOUR) / Constants.SECONDS_IN_MINUTE
        val secs = seconds % Constants.SECONDS_IN_MINUTE
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

    private fun loadPreferences() {
        val sharedPreferences = requireActivity()
            .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val workTime = sharedPreferences.getString(Constants.WORK_TIME_KEY, "")
        val restTime = sharedPreferences.getString(Constants.REST_TIME_KEY, "")
        etWorkTime.setText(workTime)
        etRestTime.setText(restTime)
    }

    private fun savePreferences() {
        val sharedPreferences = requireActivity()
            .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.WORK_TIME_KEY, etWorkTime.text.toString())
        editor.putString(Constants.REST_TIME_KEY, etRestTime.text.toString())
        editor.apply()
    }
}
