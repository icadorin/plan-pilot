package com.israel.planpilot

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FragmentActivityList : Fragment() {
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityListView: ListView

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"
        private const val ARG_SELECTED_MONTH = "selected_month"
        private const val ARG_SELECTED_YEAR = "selected_year"

        fun newInstance(
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): FragmentActivityList {
            val fragment = FragmentActivityList()
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

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
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

            val adapter = object : ArrayAdapter<Activity>(
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

                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                    val startDate = LocalDate.parse(activity?.startDate)
                    val endDate = LocalDate.parse(activity?.endDate)
                    activityStartDate.text = startDate.format(formatter)
                    activityEndDate.text = endDate.format(formatter)

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
                        }
                    }
                    .setNegativeButton("Editar", null)
                    .show()
                true
            }
        }
    }
}
