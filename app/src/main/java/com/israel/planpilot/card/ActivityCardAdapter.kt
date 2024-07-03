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
import com.israel.planpilot.model.ActivityCardModel
import com.israel.planpilot.repository.ActivityCardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityCardAdapter(
    private val cardRepository: ActivityCardRepository,
    private val scope: CoroutineScope
) : ListAdapter<ActivityCardModel, ActivityCardAdapter.ActivityViewHolder>(ActivityCardDiffCallback()) {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityNameTextView: TextView = itemView.findViewById(R.id.textViewActivityName)
        val activityDateTextView: TextView = itemView.findViewById(R.id.textViewActivityDate)
        val checkButton: ImageButton = itemView.findViewById(R.id.buttonCheck)
        val uncheckButton: ImageButton = itemView.findViewById(R.id.buttonUncheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activityCard = getItem(position)
        holder.activityNameTextView.text = activityCard.activityName
        holder.activityDateTextView.text = activityCard.date?.let { formatDate(it) }

        holder.checkButton.setOnClickListener {
            scope.launch {
                cardRepository.updateStatusActivityCard(activityCard, true)
                activityCard.completed = true
                withContext(Dispatchers.Main) {
                    submitList(currentList.filterIndexed { index, _ -> index != position })
                }
            }
        }

        holder.uncheckButton.setOnClickListener {
            scope.launch {
                cardRepository.updateStatusActivityCard(activityCard, false)
                activityCard.completed = false
                withContext(Dispatchers.Main) {
                    submitList(currentList.filterIndexed { index, _ -> index != position })
                }
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return dateString
    }

    class ActivityCardDiffCallback : DiffUtil.ItemCallback<ActivityCardModel>() {
        override fun areItemsTheSame(oldItem: ActivityCardModel, newItem: ActivityCardModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityCardModel, newItem: ActivityCardModel): Boolean {
            return oldItem == newItem
        }
    }
}
