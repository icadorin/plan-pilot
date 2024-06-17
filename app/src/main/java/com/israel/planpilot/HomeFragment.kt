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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var repository: ActivityRepository
    private lateinit var cardRepository: ActivityCardRepository
    private var cachedActivities: List<Activity>? = null
    private var cachedCards: List<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        repository = ActivityRepository()
        cardRepository = ActivityCardRepository()

        displayActivities(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (cachedActivities == null) {
            loadActivitiesAndGenerateCards(view)
        } else {
            cachedCards?.let { displayCachedCards(view, it) }
        }
    }

    private fun loadActivitiesAndGenerateCards(view: View) {
        repository.readAllActivities { activities ->
            val filteredActivities = filterActivities(activities)
            view.post {
                generateCards(view, filteredActivities)
            }
        }
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

    private fun generateCards(view: View, activities: List<Activity>) {
        val linearLayoutContainer = view.findViewById<LinearLayout>(R.id.linearlayoutCardsContainer)
        linearLayoutContainer.removeAllViews()

        val generatedCards = mutableListOf<View>()

        activities.forEach { activity ->
            val cardView = layoutInflater.inflate(
                R.layout.card_activity,
                linearLayoutContainer,
                false
            ) as CardView

            lifecycleScope.launch {
                val existingCard = cardRepository.getActivityCardByActivityId(activity.id)

                if (existingCard == null) {
                    val activityNameTextView = cardView.findViewById<TextView>(R.id.textViewActivityName)
                    val activityDateTextView = cardView.findViewById<TextView>(R.id.textViewActivityDate)
                    val checkButton = cardView.findViewById<ImageButton>(R.id.buttonCheck)
                    val uncheckButton = cardView.findViewById<ImageButton>(R.id.buttonUncheck)

                    val startDate = activity.startDate?.let { LocalDate.parse(it) }
                    val endDate = activity.endDate?.let { LocalDate.parse(it) }

                    val formattedStartDate = startDate?.let { DateFormatterUtils.formatLocalDateToString(it) }
                    val formattedEndDate = endDate?.let { DateFormatterUtils.formatLocalDateToString(it) }
                    val formattedString = getString(R.string.activity_date, formattedStartDate, formattedEndDate)

                    activityNameTextView.text = activity.name
                    activityDateTextView.text = formattedString
                    val selectedLocalDate = LocalDate.now()
                    val dateString = DateFormatterUtils.formatLocalDateToString(selectedLocalDate)

                    checkButton.setOnClickListener {
                        activity.alarmTriggerTime?.let { it1 ->
                            createAndSaveActivityCard(activity.id, activity.name, it1, true, dateString)
                            loadActivitiesAndGenerateCards(view)
                        }
                    }

                    uncheckButton.setOnClickListener {
                        activity.alarmTriggerTime?.let { it1 ->
                            createAndSaveActivityCard(activity.id, activity.name, it1, false, dateString)
                            loadActivitiesAndGenerateCards(view)
                        }
                    }

                    val margin = resources.getDimensionPixelSize(R.dimen.card_margin)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(margin, margin, margin, margin)

                    cardView.layoutParams = layoutParams

                    cardView.parent?.let {
                        (it as ViewGroup).removeView(cardView)
                    }

                    linearLayoutContainer.addView(cardView)
                    generatedCards.add(cardView)
                }
            }
        }

        cachedCards = generatedCards
        generatedCards.forEach { linearLayoutContainer.addView(it) }
    }

    private fun createAndSaveActivityCard(
        activityId: String,
        activityName: String,
        alarmTriggerTime: String,
        completed: Boolean,
        dateString: String
    ) {
        val activityCard = ActivityCard(
            id = generateUniqueId().toString(),
            activityId = activityId,
            activityName = activityName,
            alarmTriggerTime = alarmTriggerTime,
            completed = completed,
            date = dateString
        )

        lifecycleScope.launch {
            cardRepository.addActivityCard(activityCard)
        }
    }

    private fun displayCachedCards(view: View, cards: List<View>) {
        val linearLayoutContainer = view.findViewById<LinearLayout>(R.id.linearlayoutCardsContainer)
        linearLayoutContainer.removeAllViews()

        cards.forEach { linearLayoutContainer.addView(it) }
    }

    private fun generateUniqueId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
}

