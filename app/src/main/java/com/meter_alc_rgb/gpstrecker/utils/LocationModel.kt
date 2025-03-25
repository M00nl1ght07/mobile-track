package com.meter_alc_rgb.gpstrecker.utils

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.io.Serializable

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val accuracy: Float = 0.0f,
    val polyline: ArrayList<GeoPoint>
) : Serializable
