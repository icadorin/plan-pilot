package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var repository: ActivityRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        repository = ActivityRepository()
        loadActivitiesAndGenerateCards(view)

        val textViewMonthYear = view.findViewById<TextView>(R.id.textViewMonthYear)
        val textViewDay = view.findViewById<TextView>(R.id.textViewDay)
        val currentDate = LocalDate.now()
        textViewMonthYear.text = currentDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
        textViewDay.text = currentDate.format(DateTimeFormatter.ofPattern("dd"))

        val textViewActivityName = view.findViewById<TextView>(R.id.textViewActivityName)

        loadActivitiesAndDisplayNames(textViewActivityName)

        return view
    }

    private fun loadActivitiesAndGenerateCards(view: View) {
        repository.readAllActivities { activities ->
            val filteredActivities = filterActivities(activities)
            generateCards(view, filteredActivities)
        }
    }

    private fun loadActivitiesAndDisplayNames(textViewActivityName: TextView) {
        repository.readAllActivities { activities ->
            val filteredActivities = filterActivities(activities)
            displayActivityNames(textViewActivityName, filteredActivities)
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

    private fun displayActivityNames(textViewActivityName: TextView, activities: List<Activity>) {
        val maxActivities = 3
        var activitiesCount = 0
        val activitiesStringBuilder = StringBuilder()

        activities.forEach { activity ->
            if (activitiesCount < maxActivities) {
                if (activitiesCount > 0) {
                    activitiesStringBuilder.append(", ")
                }
                activitiesStringBuilder.append(activity.name)
                activitiesCount++
            }
        }

        if (activities.size > maxActivities) {
            activitiesStringBuilder.append("...")
        }

        textViewActivityName.text = activitiesStringBuilder.toString()
    }

    private fun generateCards(view: View, activities: List<Activity>) {
        val linearLayoutContainer = view.findViewById<LinearLayout>(R.id.linearlayoutCardsContainer)

        activities.forEach { activity ->
            val cardView = layoutInflater.inflate(R.layout.card_activity, null) as CardView
            val activityNameTextView = cardView.findViewById<TextView>(R.id.textViewActivityName)
            val activityDateTextView = cardView.findViewById<TextView>(R.id.textViewActivityDate)
            val checkButton = cardView.findViewById<ImageButton>(R.id.buttonCheck)
            val uncheckButton = cardView.findViewById<ImageButton>(R.id.buttonUncheck)

            val startDate = activity.startDate?.let { LocalDate.parse(it) }
            val endDate = activity.endDate?.let { LocalDate.parse(it) }

            val formattedStartDate = startDate?.let { DateFormatterUtils.formatLocalDateToString(it) }
            val formattedEndDate = endDate?.let { DateFormatterUtils.formatLocalDateToString(it) }

            activityNameTextView.text = activity.name
            activityDateTextView.text = "${formattedStartDate} • ${formattedEndDate}"

            checkButton.setOnClickListener {
                //
            }

            uncheckButton.setOnClickListener {
                //
            }

            val margin = resources.getDimensionPixelSize(R.dimen.card_margin)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(margin, margin, margin, margin)

            cardView.layoutParams = layoutParams
            linearLayoutContainer.addView(cardView)
        }
    }
}
