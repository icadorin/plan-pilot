package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var repository: ActivityRepository
    private var cachedActivities: List<Activity>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        repository = ActivityRepository()

        displayActivities(view)

        return view
    }

    private fun displayActivities(view: View) {
        val activityNameFirst = view.findViewById<TextView>(R.id.activityNameFirst)
        val hourFirst = view.findViewById<TextView>(R.id.hourFirst)

        val activityNameSecond = view.findViewById<TextView>(R.id.activityNameSecond)
        val hourSecond = view.findViewById<TextView>(R.id.hourSecond)

        val activityNameThird = view.findViewById<TextView>(R.id.activityNameThird)
        val hourThird = view.findViewById<TextView>(R.id.hourThird)

        repository.readAllActivities { activities ->
            val filteredActivities = filterActivities(activities)

            activityNameFirst?.post {
                if (filteredActivities.isNotEmpty()) {
                    activityNameFirst.text = filteredActivities.getOrElse(0) { Activity() }.name
                    hourFirst.text = filteredActivities.getOrElse(0) { Activity() }.alarmTriggerTime
                } else {
                    activityNameFirst.text = ""
                    hourFirst.text = ""
                }
            }

            activityNameSecond?.post {
                if (filteredActivities.size > 1) {
                    activityNameSecond.text = filteredActivities.getOrElse(1) { Activity() }.name
                    hourSecond.text = filteredActivities.getOrElse(1) { Activity() }.alarmTriggerTime
                } else {
                    activityNameSecond.text = ""
                    hourSecond.text = ""
                }
            }

            activityNameThird?.post {
                if (filteredActivities.size > 2) {
                    activityNameThird.text = filteredActivities.getOrElse(2) { Activity() }.name
                    hourThird.text = filteredActivities.getOrElse(2) { Activity() }.alarmTriggerTime
                } else {
                    activityNameThird.text = ""
                    hourThird.text = ""
                }
            }
        }
    }

    private fun filterActivities(activities: List<Activity>): List<Activity> {
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
