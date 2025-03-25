package com.meter_alc_rgb.gpstrecker.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.database.TrackItem
import com.meter_alc_rgb.gpstrecker.databinding.SaveTrackDialogBinding

object DialogManager {

    fun showSaveTrackDialog(context: Context, item: TrackItem, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val binding = SaveTrackDialogBinding.inflate(LayoutInflater.from(context),
            null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            val distance = "Расстояние: ${item.distance} км"
            val time = "Время: ${item.time} ч"
            val velocity = "Скорость: ${item.velocity} км / ч"
            tvDistance.text = distance
            tvTime.text = time
            tvVelocity.text = velocity
            bSave.setOnClickListener {
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener {
               dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(null)
        dialog.show()
    }

    fun showLocationEnableDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Location is disabled!")
        dialog.setMessage("Do you want enable location?")
        dialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            "No"
        ) { _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "Yes"
        ) { _, _ ->
            listener.onClick()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick()
    }
}