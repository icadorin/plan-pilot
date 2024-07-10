package com.israel.planpilot.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.israel.planpilot.MainActivity
import com.israel.planpilot.R
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityRepository
import com.israel.planpilot.utils.DateFormatterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

class ListTodayActivitiesFragment : Fragment() {
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityListView: ListView
    private lateinit var adapter: ArrayAdapter<ActivityModel>

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"
        private const val ARG_SELECTED_MONTH = "selected_month"
        private const val ARG_SELECTED_YEAR = "selected_year"

        fun newInstance(
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): ListTodayActivitiesFragment {
            val fragment = ListTodayActivitiesFragment()
            val args = Bundle()
            args.putInt(ARG_SELECTED_DAY, selectedDay)
            args.putInt(ARG_SELECTED_MONTH, selectedMonth)
            args.putInt(ARG_SELECTED_YEAR, selectedYear)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)
        val selectedMonth = arguments?.getInt(ARG_SELECTED_MONTH)
        val selectedYear = arguments?.getInt(ARG_SELECTED_YEAR)

        val monthNames = arrayOf(
            "JAN", "FEV", "MAR", "ABR",
            "MAI", "JUN", "JUL", "AGO",
            "SET", "OUT", "NOV", "DEZ"
        )

        val monthName = monthNames[selectedMonth!! - 1]

        view.findViewById<TextView>(R.id.selectedMonth).text = monthName
        view.findViewById<TextView>(R.id.selectedYear).text = selectedYear.toString()
        view.findViewById<TextView>(R.id.selectedDay).text = selectedDay.toString()

        activityRepository = ActivityRepository()
        activityListView = view.findViewById(R.id.activity_list_view)

        loadActivities()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun loadActivities() {
        val context = context ?: return

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)
        val selectedMonth = arguments?.getInt(ARG_SELECTED_MONTH)
        val selectedYear = arguments?.getInt(ARG_SELECTED_YEAR)

        val selectedDate = LocalDate.of(selectedYear!!, selectedMonth!!, selectedDay!!)

        activityRepository.readAllActivities { activities ->
            val filteredActivities = activities.filter { activity ->
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

            adapter = object : ArrayAdapter<ActivityModel>(
                context,
                R.layout.activity_item,
                filteredActivities
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView
                        ?: LayoutInflater
                            .from(context)
                            .inflate(R.layout.activity_item, parent, false)

                    val activityName = view.findViewById<TextView>(R.id.activityName)
                    val activityStartDate = view.findViewById<TextView>(R.id.activityStartDate)
                    val activityEndDate = view.findViewById<TextView>(R.id.activityEndDate)

                    val activity = getItem(position)
                    activityName.text = activity?.name

                    val startDate = LocalDate.parse(activity?.startDate)
                    val endDate = LocalDate.parse(activity?.endDate)
                    activityStartDate.text = DateFormatterUtils.formatLocalDateToString(startDate)
                    activityEndDate.text = DateFormatterUtils.formatLocalDateToString(endDate)

                    return view
                }
            }
            activityListView.adapter = adapter

            activityListView.setOnItemLongClickListener { _, _, position, _ ->
                val activity = adapter.getItem(position)
                AlertDialog.Builder(context)
                    .setTitle("Opções")
                    .setMessage("Escolha uma opção para a atividade ${activity?.name}")
                    .setPositiveButton("Deletar") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            activityRepository.deleteActivity(activity?.id.toString())
                            withContext(Dispatchers.Main) {
                                reloadActivities()
                            }
                        }
                    }
                    .setNegativeButton("Editar") { _, _ ->
                        val selectedActivity = adapter.getItem(position)
                        val fragment = EditActivityFragment().apply {
                            arguments = Bundle().apply {
                                putString("activityId", selectedActivity?.id.toString())
                            }
                        }
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.nav_host_fragment, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    .show()
                true
            }
        }
    }

    private fun reloadActivities() {
        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)
        val selectedMonth = arguments?.getInt(ARG_SELECTED_MONTH)
        val selectedYear = arguments?.getInt(ARG_SELECTED_YEAR)

        val selectedDate = LocalDate.of(selectedYear!!, selectedMonth!!, selectedDay!!)

        activityRepository.readAllActivities { activities ->
            val filteredActivities = activities.filter { activity ->
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

            adapter.clear()
            adapter.addAll(filteredActivities)
            adapter.notifyDataSetChanged()
        }
    }
}
