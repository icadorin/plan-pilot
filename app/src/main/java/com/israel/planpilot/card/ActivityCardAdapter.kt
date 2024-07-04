package com.israel.planpilot.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.R
import com.israel.planpilot.TrackActivityViewModel
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.repository.ActivityCardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

