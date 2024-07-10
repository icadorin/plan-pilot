package com.israel.planpilot

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityRepository
import kotlinx.coroutines.launch

class TrackActivityFragment : Fragment() {

    private lateinit var recyclerViewActivities: RecyclerView
    private lateinit var activityAdapter: ActivityAdapter
    private val viewModel: TrackActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_track_activity, container, false)

        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities)
        activityAdapter = ActivityAdapter()

        recyclerViewActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
        }

        observeViewModel()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchTodayActivities()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.todayActivities.observe(viewLifecycleOwner) { activities ->
            activityAdapter.submitList(activities)
        }
    }

    class ActivityAdapter :
        ListAdapter<ActivityModel, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track_activity, parent, false)
            return ActivityViewHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val activityName: TextView = itemView.findViewById(R.id.activityName)
            private val activityTime: TextView = itemView.findViewById(R.id.activityTime)

            fun bind(activity: ActivityModel) {
                activityName.text = activity.name
                activityTime.text = activity.alarmTriggerTime
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityModel>() {
        override fun areItemsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
            return oldItem == newItem
        }
    }

    class TrackActivityViewModel(application: Application) : AndroidViewModel(application) {
        private val activityRepository = ActivityRepository()

        private val _todayActivities = MutableLiveData<List<ActivityModel>>()
        val todayActivities: LiveData<List<ActivityModel>> get() = _todayActivities

        private val trace: Trace = FirebasePerformance.getInstance().newTrace("fetch_today_activities")

        init {
            _todayActivities.value = emptyList()
        }

        fun fetchTodayActivities() {
            trace.start()

            viewModelScope.launch {
                activityRepository.readTodayActivities { activities ->
                    _todayActivities.postValue(activities)
                    trace.stop()
                }
            }
        }
    }
}
