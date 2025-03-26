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
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_message))
        dialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            context.getString(R.string.no)
        ) { _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            context.getString(R.string.yes)
        ) { _, _ ->
            listener.onClick()
        }
        dialog.show()
    }
    
    /**
     * Показывает диалог с объяснением необходимости фонового доступа к местоположению
     * Требуется для Android 11+ (API 30+), так как разрешение ACCESS_BACKGROUND_LOCATION
     * должно запрашиваться отдельно с объяснением.
     * 
     * @param context Контекст для создания диалога
     * @param listener Слушатель для обработки нажатия на кнопку
     */
    fun showBackgroundLocationDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.background_location_title)
        dialog.setMessage(context.getString(R.string.background_location_message))
        dialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            context.getString(R.string.no)
        ) { _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            context.getString(R.string.yes)
        ) { _, _ ->
            listener.onClick()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick()
    }
}