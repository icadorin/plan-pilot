package com.israel.planpilot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.repository.ActivityRepository
import kotlinx.coroutines.launch

class TrackActivityViewModel : ViewModel() {

    private val activityRepository = ActivityRepository()
    val activityCardRepository = ActivityCardRepository()

    private val _todayActivities = MutableLiveData<List<ActivityModel>>()
    val todayActivities: LiveData<List<ActivityModel>> = _todayActivities

    private val _activityCards = MutableLiveData<List<ActivityCardModel>>()
    val activityCards: LiveData<List<ActivityCardModel>> = _activityCards

    init {
        fetchTodayActivities()
        initializeAndFetchActivityCards()
    }

    private fun fetchTodayActivities() {
        viewModelScope.launch {
            activityRepository.readTodayActivities { activities ->
                _todayActivities.postValue(activities.take(3))
            }
        }
    }

    private fun initializeAndFetchActivityCards() {
        viewModelScope.launch {
            activityCardRepository.initializeCache()
            refreshActivityCards()
        }
    }

    fun refreshActivityCards() {
        viewModelScope.launch {
            val activityCards = activityCardRepository.getUncompletedActivityCards()
            println("Activity cards fetched no ViewModel: ${activityCards.size}")
            _activityCards.postValue(activityCards)
        }
    }

    fun forceRefreshActivityCards() {
        viewModelScope.launch {
            activityCardRepository.initializeCache()
            refreshActivityCards()
        }
    }

    fun refreshTodayActivities() {
        viewModelScope.launch {
            activityRepository.readTodayActivities { activities ->
                _todayActivities.postValue(activities.take(3))
            }
        }
    }
}
