package com.israel.planpilot.model

import androidx.lifecycle.ViewModel
import java.util.Timer

class TimerViewModel : ViewModel() {
    var mainTime: Int = 0
    var isWorking = false
    var isResting = false
    var isPaused = false
    var completeCycles = 0
    var fullWorkingTime = 0
    var numberOfIntervals = 0
    var cyclesQtdManager = ArrayList<Boolean>()
    var timer: Timer? = null
}
