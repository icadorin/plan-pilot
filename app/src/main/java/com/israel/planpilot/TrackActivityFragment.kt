package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.model.ActivityModel
import com.israel.planpilot.repository.ActivityCardRepository
import com.israel.planpilot.repository.ActivityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    class TrackActivityViewModel : ViewModel() {

        private val activityRepository = ActivityRepository()
        val activityCardRepository = ActivityCardRepository()

        private val _todayActivities = MutableLiveData<List<ActivityModel>>()
        val todayActivities: LiveData<List<ActivityModel>> = _todayActivities

        private val _activityCards = MutableLiveData<List<ActivityCardModel>>()
        val activityCards: LiveData<List<ActivityCardModel>> = _activityCards

        init {
            fetchTodayActivities()
            initializeAndFetchActivityCards()
        }

        private fun fetchTodayActivities() {
            viewModelScope.launch {
                activityRepository.readTodayActivities { activities ->
                    _todayActivities.postValue(activities.take(3))
                }
            }
        }

        private fun initializeAndFetchActivityCards() {
            viewModelScope.launch {
                activityCardRepository.initializeCache()
                refreshActivityCards()
            }
        }

        fun refreshActivityCards() {
            viewModelScope.launch {
                val activityCards = activityCardRepository.getUncompletedActivityCards()
                println("Activity cards fetched no ViewModel: ${activityCards.size}")
                _activityCards.postValue(activityCards)
            }
        }

        fun forceRefreshActivityCards() {
            viewModelScope.launch {
                activityCardRepository.initializeCache()
                refreshActivityCards()
            }
        }

        fun refreshTodayActivities() {
            viewModelScope.launch {
                activityRepository.readTodayActivities { activities ->
                    _todayActivities.postValue(activities.take(3))
                }
            }
        }
    }

    class ActivityCardAdapter(private val viewModel: TrackActivityViewModel) :
        ListAdapter<ActivityCardModel, ActivityCardAdapter.ViewHolder>(ActivityCardDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_activity, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val activityCard = getItem(position)
            holder.bind(activityCard)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val activityNameTextView: TextView = itemView.findViewById(R.id.textViewActivityName)
            private val activityDateTextView: TextView = itemView.findViewById(R.id.textViewActivityDate)
            private val uncheckButton: ImageButton = itemView.findViewById(R.id.buttonUncheck)
            private val checkButton: ImageButton = itemView.findViewById(R.id.buttonCheck)

            fun bind(activityCard: ActivityCardModel) {
                activityNameTextView.text = activityCard.activityName
                activityDateTextView.text = activityCard.date

                uncheckButton.setOnClickListener {
                    updateCompletion(activityCard.id, false)
                }

                checkButton.setOnClickListener {
                    updateCompletion(activityCard.id, true)
                }
            }

            private fun updateCompletion(activityCardId: String, completed: Boolean) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        viewModel.activityCardRepository.updateActivityCardCompletion(activityCardId, completed)
                        viewModel.refreshActivityCards()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private class ActivityCardDiffCallback : DiffUtil.ItemCallback<ActivityCardModel>() {
            override fun areItemsTheSame(oldItem: ActivityCardModel, newItem: ActivityCardModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ActivityCardModel, newItem: ActivityCardModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ActivityAdapter : ListAdapter<ActivityModel, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_activity, parent, false)
            return ActivityViewHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
            val activity = getItem(position)
            holder.bind(activity)
        }

        class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val activityName: TextView = view.findViewById(R.id.activityName)
            private val activityTime: TextView = view.findViewById(R.id.activityTime)

            fun bind(activity: ActivityModel) {
                activityName.text = activity.name
                activityTime.text = activity.alarmTriggerTime
            }
        }

        private class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityModel>() {
            override fun areItemsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ActivityModel, newItem: ActivityModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
