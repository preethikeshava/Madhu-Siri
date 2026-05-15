package com.madhusiri.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.madhusiri.app.R
import com.madhusiri.app.data.local.entity.HiveEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HiveHealthAdapter : ListAdapter<HiveEntity, HiveHealthAdapter.LogViewHolder>(LogDiffCallback()) {

    // Store expanded status tracked by Database Record Id internally
    private val expandedItemIds = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hive_health_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = getItem(position)
        val isExpanded = expandedItemIds.contains(log.id)
        holder.bind(log, isExpanded) {
            // Toggle local tracking item visibility layout layout block state
            if (isExpanded) {
                expandedItemIds.remove(log.id)
            } else {
                expandedItemIds.add(log.id)
            }
            notifyItemChanged(position)
        }
    }

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textScoreNum: TextView = itemView.findViewById(R.id.text_health_score_number)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val textHoney: TextView = itemView.findViewById(R.id.text_honey_production)
        private val textNotes: TextView = itemView.findViewById(R.id.text_notes)
        private val viewStatusBar: View = itemView.findViewById(R.id.view_health_indicator_bar)
        private val iconExpand: ImageView = itemView.findViewById(R.id.icon_expand)
        private val layoutExpandable: View = itemView.findViewById(R.id.layout_expandable_notes)

        fun bind(log: HiveEntity, isExpanded: Boolean, onExpandToggle: () -> Unit) {
            val scoreInt = log.healthScore.toInt()
            textScoreNum.text = scoreInt.toString()
            
            // Dynamic Color Bar binding driven directly by parsed metric value
            val context = itemView.context
            val statusColorRes = when {
                scoreInt >= 8 -> R.color.status_active
                scoreInt >= 5 -> R.color.status_warning
                else -> R.color.danger_red
            }
            viewStatusBar.setBackgroundColor(context.getColor(statusColorRes))
            textScoreNum.setTextColor(context.getColor(statusColorRes))

            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            textDate.text = dateFormat.format(Date(log.lastInspectionDate))
            
            textHoney.text = "🍯 Yield: ${log.honeyProductionKg} kg"
            textNotes.text = if (log.notes.isNullOrBlank()) "No inspection notes recorded." else log.notes

            // Layout states visibility expansion block tracking
            layoutExpandable.visibility = if (isExpanded) View.VISIBLE else View.GONE
            iconExpand.setImageResource(
                if (isExpanded) android.R.drawable.arrow_up_float 
                else android.R.drawable.arrow_down_float
            )

            // Click listener wrapper triggers state transformations
            itemView.setOnClickListener { onExpandToggle() }
            iconExpand.setOnClickListener { onExpandToggle() }
        }
    }

    class LogDiffCallback : DiffUtil.ItemCallback<HiveEntity>() {
        override fun areItemsTheSame(oldItem: HiveEntity, newItem: HiveEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HiveEntity, newItem: HiveEntity): Boolean {
            return oldItem == newItem
        }
    }
}
