package com.meter_alc_rgb.gpstrecker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
@SuppressLint("SimpleDateFormat")
object LocationUtils {
    private val formatter = SimpleDateFormat("HH:mm:ss")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
    fun getTime(time: Long): String{
        val  cv = Calendar.getInstance()
        formatter.timeZone = TimeZone.getTimeZone("UTC") // sin UTC empieza desde 01:00:00
        cv.timeInMillis = time
        return formatter.format(cv.time)
    }
    fun getCurrentDate(): String{
        val  cv = Calendar.getInstance()
        return dateFormatter.format(cv.time)
    }
}