package com.israel.planpilot

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentActivitiesList : Fragment() {
    private lateinit var activityRepository: ActivityRepository
    private lateinit var activityListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities_list, container, false)

        val mainActivity = activity as MainActivity
        mainActivity.btnActivitiesList.visibility = View.GONE

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
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
            val adapter = object : ArrayAdapter<Activity>(
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
                    "${activity?.startDate}".also { activityStartDate.text = it }
                    "${activity?.endDate}".also { activityEndDate.text = it }

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

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as MainActivity
        mainActivity.btnActivitiesList.visibility = View.VISIBLE
    }
}