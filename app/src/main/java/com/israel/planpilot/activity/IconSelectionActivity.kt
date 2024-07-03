package com.israel.planpilot.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.israel.planpilot.R
import com.israel.planpilot.utils.Constants

class IconSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_selection)

        val recyclerView = findViewById<RecyclerView>(R.id.iconRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        val iconAdapter = IconAdapter { selectedIconResource ->
            val resultIntent = Intent()
            resultIntent.putExtra(Constants.SELECTED_ICON_EXTRA, selectedIconResource)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        recyclerView.adapter = iconAdapter
    }

    inner class IconAdapter(private val onIconClickListener: (Int) -> Unit) :
        RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

        inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            val view = layoutInflater.inflate(R.layout.icon_item, parent, false)
            return IconViewHolder(view)
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            val iconResource = when (position) {
                0 -> R.drawable.star_icon
                1 -> R.drawable.dataset_icon
                else -> 0
            }
            val iconImageView = holder.itemView.findViewById<ImageView>(R.id.iconImageView)
            iconImageView.setImageResource(iconResource)
            iconImageView.setOnClickListener {
                onIconClickListener(iconResource)
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}
