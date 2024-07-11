package com.israel.planpilot

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityRepository
import com.israel.planpilot.utils.Constants.CELLS_PER_PAGE
import com.israel.planpilot.utils.Constants.END_YEAR
import com.israel.planpilot.utils.Constants.HORIZONTAL_CELLS
import com.israel.planpilot.utils.Constants.MONTHS_IN_YEAR
import com.israel.planpilot.utils.Constants.START_YEAR
import com.israel.planpilot.utils.Constants.VERTICAL_CELLS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MonthlyCalendarFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var selectedDate: Date
    private lateinit var currentDate: Calendar
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var activityRepository: ActivityRepository
    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var allActivities = listOf<ActivityModel>()
    private var activitiesHash: Int? = null
    private var userId: String? = null

    private inner class DayItem(
        val day: String,
        val isCurrentMonth: Boolean,
        val year: Int,
        val month: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_large_mon_cal, container, false)
        currentDate = Calendar.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        initViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        coroutineScope = CoroutineScope(Dispatchers.Main)
        activityRepository = ActivityRepository()
        toolbar = requireActivity().findViewById(R.id.custom_toolbar)
        (activity as MainActivity).showReturnToTodayButton()
        setupReturnToTodayButton()

        userId?.let { id ->
            activityRepository.readAllActivities(id) { activities ->
                allActivities = activities
                updateCalendarData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupMainActivity()
        calculateDate()
        updateCalendarData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isAdded) {
            coroutineScope.cancel()
        }
        (activity as MainActivity).hideReturnToTodayButton()
    }

    private fun initViews(view: View) {
        initWeekdayRecyclerView(view)
        initCalendarViewPager(view)
    }

    private fun setupMainActivity() {
        (activity as MainActivity).apply {
            showReturnToTodayButton()
        }
    }

    private fun calculateDate() {
        val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentPagePosition = sharedPreferences?.getInt("lastPagePosition", 0) ?: 0
        val year = START_YEAR + currentPagePosition / MONTHS_IN_YEAR
        val month = (currentPagePosition % MONTHS_IN_YEAR) + 1

        updateToolbar(year, month)
    }

    private fun initCalendarViewPager(view: View) {

        viewPager = view.findViewById(R.id.viewPager)
        selectedDate = Calendar.getInstance().time
        viewPager.setPageTransformer(CustomPageTransformer())

        val startDate = Calendar.getInstance()
        val startYear = START_YEAR
        val currentYear = startDate.get(Calendar.YEAR)
        val currentMonth = startDate.get(Calendar.MONTH)
        val initialPosition = (currentYear - startYear) * MONTHS_IN_YEAR + currentMonth
        val adapter = CalendarPagerAdapter()
        viewPager.adapter = adapter

        val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val lastPagePosition = sharedPreferences?.getInt("lastPagePosition", initialPosition)

        lastPagePosition?.let { viewPager.setCurrentItem(it, false) }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val year = START_YEAR + position / MONTHS_IN_YEAR
                val month = position % MONTHS_IN_YEAR + 1

                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                selectedDate = calendar.time

                updateToolbar(year, month)

                sharedPreferences?.edit()?.apply {
                    putInt("lastPagePosition", position)
                    apply()
                }
            }
        })
    }

    private fun updateCalendarData() {
        coroutineScope.launch {
            getUpdatedActivities { activities ->
                val newHash = activities.hashCode()
                if (newHash!= activitiesHash) {
                    activitiesHash = newHash
                    allActivities = activities
                    val diffResult = DiffUtil.
                        calculateDiff(ActivitiesDiffCallback(allActivities, allActivities))
                    diffResult.dispatchUpdatesTo((viewPager.adapter as CalendarPagerAdapter))
                }
            }
        }
    }

    private fun getUpdatedActivities(onSuccess: (List<ActivityModel>) -> Unit) {
        if (allActivities.isEmpty()) {
            userId?.let { id ->
                activityRepository.readAllActivities(id) { activities ->
                    allActivities = activities
                    onSuccess(activities)
                    println("Atividades recuperadas: $activities")
                }
            }
        } else {
            onSuccess(allActivities)
            println("Atividades já em cache: $allActivities")
        }
    }

    private fun setupReturnToTodayButton() {
        val btnReturnToToday: ImageButton = toolbar.findViewById(R.id.btnReturnToToday)

        btnReturnToToday.setOnClickListener {
            selectedDate = Calendar.getInstance().time

            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val initialPosition = (currentYear - START_YEAR) * MONTHS_IN_YEAR + currentMonth

            viewPager.setCurrentItem(initialPosition, false)

            val month = currentMonth + 1
            updateToolbar(currentYear, month)
        }
    }

    private fun updateToolbar(year: Int, month: Int) {
        val adjustedMonth = month - 1

        val calendar = Calendar.getInstance()
        calendar.set(year, adjustedMonth, 1)

        val monthString = calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale.getDefault()
        )?.substring(0, 3)?.uppercase(Locale.getDefault())

        toolbar.title = "$monthString. $year"
    }

    private fun initWeekdayRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val weekdays = arrayOf("D", "S", "T", "Q", "Q", "S", "S")

        val adapterWeekday = WeekdayAdapter(requireContext(), weekdays)

        recyclerView.layoutManager = GridLayoutManager(context, HORIZONTAL_CELLS)
        recyclerView.adapter = adapterWeekday

        val horizontalDivider = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.HORIZONTAL
        )

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_horizontal_week_days)
            ?.let { horizontalDivider.setDrawable(it) }
        recyclerView.addItemDecoration(horizontalDivider)
    }

    private inner class CalendarPagerAdapter :
        RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_page, parent, false)

            return CalendarViewHolder(view, parent.context)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            val year = START_YEAR + position / MONTHS_IN_YEAR
            val month = position % MONTHS_IN_YEAR + 1
            val currentMonthDays = getDaysInMonth(year, month)
            holder.setDays(currentMonthDays)
        }

        override fun getItemCount(): Int {
            return (END_YEAR - START_YEAR + 1) * MONTHS_IN_YEAR
        }

        private inner class CalendarViewHolder(
            itemView: View,
            context: Context
        ) : RecyclerView.ViewHolder(itemView) {
            private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewDays)
            private val adapter = CalendarAdapter()

            init {
                recyclerView.layoutManager = GridLayoutManager(context, HORIZONTAL_CELLS)
                recyclerView.adapter = adapter
            }

            fun setDays(newDays: List<DayItem>) {
                adapter.setDays(newDays)
            }
        }
    }

    private fun getDaysInMonth(inputYear: Int, inputMonth: Int): List<DayItem> {
        val daysInMonth = mutableListOf<DayItem>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, inputYear)
        calendar.set(Calendar.MONTH, inputMonth - 1)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        if (firstDayOfWeek != 0) {
            calendar.add(Calendar.DATE, -firstDayOfWeek)
        }

        for (i in 1..firstDayOfWeek) {
            var prevMonth = inputMonth - 1
            var year = inputYear
            if (prevMonth < 1) {
                prevMonth += MONTHS_IN_YEAR
                year--
            }
            daysInMonth.add(
                DayItem(
                    calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    false,
                    year,
                    prevMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        calendar.set(Calendar.YEAR, inputYear)
        calendar.set(Calendar.MONTH, inputMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (dayOfMonth in 1..lastDayOfMonth) {
            daysInMonth.add(
                DayItem(
                    dayOfMonth.toString(),
                    true,
                    inputYear,
                    inputMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        val daysLeft = CELLS_PER_PAGE - daysInMonth.size
        for (i in 1..daysLeft) {
            var nextMonth = inputMonth + 1
            var year = inputYear
            if (nextMonth > MONTHS_IN_YEAR) {
                nextMonth -= MONTHS_IN_YEAR
                year++
            }
            daysInMonth.add(
                DayItem(
                    calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    false,
                    year,
                    nextMonth
                )
            )
            calendar.add(Calendar.DATE, 1)
        }

        return daysInMonth
    }

    private inner class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

        private var days: List<DayItem> = emptyList()

        init {
            setHasStableIds(true)
        }

        fun setDays(newDays: List<DayItem>) {
            val diffResult = DiffUtil.calculateDiff(DiffCallback(days, newDays))
            days = newDays
            diffResult.dispatchUpdatesTo(this)
        }

        override fun getItemId(position: Int): Long {
            return days[position].hashCode().toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day_large, parent, false)

            val viewPagerHeight = viewPager.measuredHeight
            val itemHeight = viewPagerHeight / VERTICAL_CELLS
            val params = view.layoutParams
            params.height = itemHeight
            view.layoutParams = params

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dayItem = days[position]
            holder.bind(dayItem)

            holder.itemView.setOnClickListener {
                val selectedDay = dayItem.day.toInt()
                val selectedMonth = dayItem.month
                val selectedYear = dayItem.year

                val action = MonthlyCalendarFragmentDirections
                    .actionLargeMonCalFragmentToFragmentActivityList(
                        selectedDay,
                        selectedMonth,
                        selectedYear
                    )
                findNavController().navigate(action)
            }
        }

        private suspend fun getActivitiesForSelectedDate(selectedDate: LocalDate): List<ActivityModel> {
            return coroutineScope.async(Dispatchers.IO) {
                allActivities.filter { activity ->
                    val startDate = LocalDate.parse(activity.startDate)
                    val endDate = LocalDate.parse(activity.endDate)
                    val activityWeekDays = activity.weekDays

                    val isStartDate = selectedDate.isEqual(startDate)
                    val isBetween = selectedDate.isAfter(startDate) && selectedDate.isBefore(endDate)
                    val isEndDate = selectedDate.isEqual(endDate)

                    val isDateInRange = isStartDate || isBetween || isEndDate

                    val isDayOfWeekInActivityWeekDays =
                        activityWeekDays?.contains(
                            selectedDate.dayOfWeek.toString().lowercase(
                                Locale.ROOT
                            )
                        ) == true

                    isDateInRange && isDayOfWeekInActivityWeekDays
                }
            }.await()
        }

        override fun getItemCount(): Int {
            return days.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val colorBlack = ContextCompat.getColor(itemView.context, R.color.black)
            private val colorGray = ContextCompat.getColor(itemView.context, R.color.gray)
            private val activityTextViews = mutableListOf<TextView>()
            val activitiesContainer: LinearLayout = itemView.findViewById(R.id.activitiesContainer)
            val textView: TextView = itemView.findViewById(R.id.textDay)

            fun bind(dayItem: DayItem) {
                textView.text = dayItem.day

                val textColor = if (dayItem.isCurrentMonth) colorBlack else colorGray
                textView.setTextColor(textColor)

                if (isCurrentDate(
                        dayItem.year,
                        dayItem.month - 1,
                        dayItem.day.toInt(),
                        dayItem.isCurrentMonth)
                ) {
                    itemView.setBackgroundResource(R.color.twilightBlue)
                }

                updateActivitiesView(dayItem)
            }

            private fun updateActivitiesView(dayItem: DayItem) {
                CoroutineScope(Dispatchers.Main).launch {
                    val selectedDate = LocalDate.of(dayItem.year, dayItem.month, dayItem.day.toInt())
                    val activitiesForSelectedDate = getActivitiesForSelectedDate(selectedDate)
                    println("Atividades para a data $selectedDate: $activitiesForSelectedDate")

                    for (i in activitiesForSelectedDate.indices) {
                        val activity = activitiesForSelectedDate[i]
                        val activityTextView = if (i < activityTextViews.size) {
                            activityTextViews[i]
                        } else {
                            TextView(itemView.context).apply {
                                gravity = Gravity.CENTER
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                activityTextViews.add(this)
                            }
                        }
                        activityTextView.text = activity.name
                        if (activityTextView.parent == null) {
                            activitiesContainer.addView(activityTextView)
                        }
                    }

                    for (i in activitiesForSelectedDate.size until activityTextViews.size) {
                        activitiesContainer.removeView(activityTextViews[i])
                    }
                }
            }

            private fun isCurrentDate(
                itemYear: Int,
                itemMonth: Int,
                itemDay: Int,
                isCurrentMonth: Boolean
            ): Boolean {
                val currentDate = Calendar.getInstance()
                return currentDate.get(Calendar.YEAR) == itemYear &&
                        currentDate.get(Calendar.MONTH) == itemMonth &&
                        currentDate.get(Calendar.DAY_OF_MONTH) == itemDay &&
                        isCurrentMonth
            }
        }
    }

    private inner class DiffCallback(
        private val oldList: List<DayItem>,
        private val newList: List<DayItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].day == newList[newItemPosition].day
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private inner class ActivitiesDiffCallback(
        private val oldActivities: List<ActivityModel>,
        private val newActivities: List<ActivityModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldActivities.size
        override fun getNewListSize(): Int = newActivities.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldActivities[oldItemPosition].id == newActivities[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldActivity = oldActivities[oldItemPosition]
            val newActivity = newActivities[newItemPosition]

            return oldActivity.name == newActivity.name
        }

    }

    class CustomPageTransformer : ViewPager2.PageTransformer {
        private val dragThreshold = 0.2f

        override fun transformPage(view: View, position: Float) {
            if (position <= -dragThreshold || position >= dragThreshold) {
                view.alpha = 0.5f
            } else {
                view.alpha = 1f
            }
        }
    }
}
