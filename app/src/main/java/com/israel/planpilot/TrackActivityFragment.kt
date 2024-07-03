package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.card.ActivityCardAdapter
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.repository.ActivityRepository

class TrackActivityFragment : Fragment() {

    private lateinit var viewModel: TrackActivityViewModel
    private lateinit var activityCardAdapter: ActivityCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_track_activity, container, false)

        val repository = ActivityRepository()
        val cardRepository = ActivityCardRepository()
        val factory =
            TrackActivityViewModel.TrackActivityViewModelFactory(repository, cardRepository)
        viewModel = ViewModelProvider(this, factory).get(TrackActivityViewModel::class.java)

        setupRecyclerView(view)
        observeViewModel()

        viewModel.loadActivities()
        viewModel.loadActivityCards()

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCards)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        activityCardAdapter = ActivityCardAdapter(
            viewModel.cardRepository,
            viewLifecycleOwner.lifecycleScope
        )
        recyclerView.adapter = activityCardAdapter
    }

    private fun observeViewModel() {
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            displayActivities(activities)
        }

        viewModel.activityCards.observe(viewLifecycleOwner) { activityCards ->
            activityCardAdapter.submitList(activityCards)
        }
    }

    private fun displayActivities(activities: List<ActivityModel>) {
        val view = view ?: return
        val activityNameFirst = view.findViewById<TextView>(R.id.activityNameFirst)
        val hourFirst = view.findViewById<TextView>(R.id.hourFirst)
        val activityNameSecond = view.findViewById<TextView>(R.id.activityNameSecond)
        val hourSecond = view.findViewById<TextView>(R.id.hourSecond)
        val activityNameThird = view.findViewById<TextView>(R.id.activityNameThird)
        val hourThird = view.findViewById<TextView>(R.id.hourThird)

        setActivityDetails(activityNameFirst, hourFirst, activities, 0)
        setActivityDetails(activityNameSecond, hourSecond, activities, 1)
        setActivityDetails(activityNameThird, hourThird, activities, 2)
    }

    private fun setActivityDetails(
        activityName: TextView?,
        hour: TextView?,
        activities: List<ActivityModel>,
        index: Int
    ) {
        activityName?.text = activities.getOrNull(index)?.name ?: ""
        hour?.text = activities.getOrNull(index)?.alarmTriggerTime ?: ""
    }
}
