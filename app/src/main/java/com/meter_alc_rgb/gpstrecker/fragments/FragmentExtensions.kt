package com.meter_alc_rgb.gpstrecker.fragments

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment

fun Fragment.checkPermission(permission: String):Boolean{
    return when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            activity as AppCompatActivity,
            permission)
        -> true
        else -> false
    }
}
fun Fragment.showToast(idRes: Int){
    Toast.makeText(activity, idRes, Toast.LENGTH_LONG).show()
}
fun Fragment.changeDrawableColor(drawable: Drawable, color: Int): Drawable{
    val wDrawable = DrawableCompat.wrap(drawable)
    DrawableCompat.setTint(wDrawable, color)
    return wDrawable
}
fun Fragment.showToast(message: String){
    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
}