package com.meter_alc_rgb.gpstrecker.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.databinding.TrackItemBinding

class TrackAdapter(private val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.TrackHolder>(Comparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackHolder(view, listener)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
         holder.bind(getItem(position))
    }

    class TrackHolder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val binding = TrackItemBinding.bind(view)
        private var item: TrackItem? = null
        init {
            itemView.setOnClickListener(this)
            binding.ibDelete.setOnClickListener(this)
        }
        fun bind(track: TrackItem) = with(binding) {
            item = track
            val time = "Время: ${track.time} м"
            val velocity = "Расстояние: ${track.velocity} км / ч"
            val distance = "${track.distance} км"
            tvDate.text = track.date
            tvTrackTime.text = time
            tvDistance.text = distance
            tvAverageVelocity.text = velocity
        }

        override fun onClick(v: View) {
            if(v.id == R.id.ibDelete){
                item?.let { listener.onClick(it, ClickType.DELETE) }
            } else {
                item?.let { listener.onClick(it, ClickType.OPEN) }
            }
        }
    }

    class Comparator : DiffUtil.ItemCallback<TrackItem>(){
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener{
        fun onClick(item: TrackItem, type: ClickType)
    }

    enum class ClickType{
        DELETE,
        OPEN
    }
}