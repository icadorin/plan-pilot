package com.israel.planpilot.card


import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.repository.ActivityRepository
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class CreateActivityCard @Inject constructor() {

    suspend fun createCardsForCurrentDate() {
        val activityRepository = ActivityRepository()
        ActivityCardRepository()

        val today = LocalDate.now()
        val activities = activityRepository.getAllActivities()
        val filteredActivities = filterActivities(activities)

        filteredActivities.forEach { activity ->
            createActivityCard(activity, today)
        }
    }

    private suspend fun createActivityCard(activity: ActivityModel, today: LocalDate) {
        val activityCardRepository = ActivityCardRepository()

        val existingCard = activityCardRepository.findActivityCardByActivityAndDate(activity.id, today.toString())
        if (existingCard == null) {
            val newCard = activity.alarmTriggerTime?.let {
                ActivityCardModel(
                    activityId = activity.id,
                    activityName = activity.name,
                    alarmTriggerTime = it,
                    completed = null,
                    date = today.toString()
                )
            }

            newCard?.let {
                activityCardRepository.addActivityCard(it)
            }
        } else {
            println("Card existente encontrado para atividade: ${activity.name} na data: $today")
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
}

