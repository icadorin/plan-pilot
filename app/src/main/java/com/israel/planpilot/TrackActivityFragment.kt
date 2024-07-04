package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.activity.ActivityAdapter
import com.israel.planpilot.card.ActivityCardAdapter

class TrackActivityFragment : Fragment() {

    private val viewModel: TrackActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_track_activity, container, false)

        val recyclerViewActivities: RecyclerView = view.findViewById(R.id.recyclerViewActivities)
        recyclerViewActivities.layoutManager = LinearLayoutManager(context)

        val activityAdapter = ActivityAdapter()
        recyclerViewActivities.adapter = activityAdapter

        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            activities?.let {
                activityAdapter.submitList(it.take(3))
            }
        }

        val recyclerViewCards: RecyclerView = view.findViewById(R.id.recyclerViewCards)
        recyclerViewCards.layoutManager = LinearLayoutManager(context)

        val activityCardAdapter = ActivityCardAdapter(viewModel)
        recyclerViewCards.adapter = activityCardAdapter

        viewModel.activityCards.observe(viewLifecycleOwner) { activityCards ->
            activityCards?.let {
                activityCardAdapter.submitList(it)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceRefreshActivityCards()
        viewModel.refreshTodayActivities()
    }
}
