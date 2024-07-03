package com.israel.planpilot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.repository.ActivityRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class TrackActivityViewModel(
    private val repository: ActivityRepository,
    val cardRepository: ActivityCardRepository
) : ViewModel() {

    private val _activities = MutableLiveData<List<ActivityModel>>()
    val activities: LiveData<List<ActivityModel>> get() = _activities

    private val _activityCards = MutableLiveData<List<ActivityCardModel>>()
    val activityCards: LiveData<List<ActivityCardModel>> get() = _activityCards

    fun loadActivities() {
        viewModelScope.launch {
            repository.readAllActivities { activities ->
                _activities.value = filterActivities(activities)
            }
        }
    }

    fun loadActivityCards() {
        viewModelScope.launch {
            val activeCards = cardRepository.getActiveActivityCardsWithNullCompletion()
            _activityCards.value = activeCards
        }
    }

    private fun filterActivities(activities: List<ActivityModel>): List<ActivityModel> {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.name.lowercase(Locale.getDefault())

        return activities.filter { activity ->
            val startDate = LocalDate.parse(activity.startDate)
            val endDate = LocalDate.parse(activity.endDate)

            (startDate.isBefore(today) || startDate.isEqual(today)) &&
                    (endDate.isAfter(today) || endDate.isEqual(today)) &&
                    (activity.weekDays?.contains(dayOfWeek) ?: false)
        }
    }

    class TrackActivityViewModelFactory(
        private val repository: ActivityRepository,
        private val cardRepository: ActivityCardRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackActivityViewModel::class.java)) {
                return TrackActivityViewModel(repository, cardRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


