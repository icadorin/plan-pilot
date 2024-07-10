package com.israel.planpilot.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.israel.planpilot.MainActivity
import com.israel.planpilot.R
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ListAllActivitiesFragment : Fragment() {
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityListView: ListView
    private lateinit var adapter: ArrayAdapter<ActivityModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities_list, container, false)

        val mainActivity = activity as MainActivity
        mainActivity.btnActivitiesList.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityRepository = ActivityRepository()
        activityListView = view.findViewById(R.id.activity_list_view)

        loadActivities()
    }

    private fun loadActivities() {
        val context = context ?: return

        activityRepository.readAllActivities { activities ->
            adapter = object : ArrayAdapter<ActivityModel>(
                context,
                R.layout.activity_item,
                activities
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
                            loadActivities()
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

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as MainActivity
        mainActivity.btnActivitiesList.visibility = View.VISIBLE
    }
}