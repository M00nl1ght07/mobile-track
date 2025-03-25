package com.meter_alc_rgb.gpstrecker.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.meter_alc_rgb.gpstrecker.*
import com.meter_alc_rgb.gpstrecker.databinding.FragmentViewTrackBinding
import com.meter_alc_rgb.gpstrecker.main.MainApp
import com.meter_alc_rgb.gpstrecker.main.MyViewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


class ViewTrackFragment : BaseFragment("view_fragment") {
    private var initPoint: GeoPoint? = null
    private lateinit var mapController: IMapController
    private lateinit var binding: FragmentViewTrackBinding
    private val model: MyViewModel by activityViewModels{
        MyViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initOSM()
        binding = FragmentViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        updateTrack()
        binding.fbMyLocation.setOnClickListener{
            goToLocation()
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun updateTrack() = with(binding){
        model.liveDataTrackItem.observe(viewLifecycleOwner){
            val distance = "Distance: ${it.distance} km"
            val time = "Time: ${it.time} m"
            val velocity = "Velocity: ${it.velocity} km/h"
            tvDistance.text = distance
            tvMiddleVelocity.text = velocity
            tvStartTime.text = time
            
            val polyline = getPolylineFromList(it.geoPoints)
            if (polyline.actualPoints.isNotEmpty()) {
                initPoint = polyline.actualPoints[0]
                map.overlays.add(polyline)
                goToLocation()
                setMarkers(polyline.actualPoints)
            }
        }
    }


    private fun getPolylineFromList(points: String): Polyline {
        val defaultColor = "#0017EF"
        val colorStr = PreferenceManager.getDefaultSharedPreferences(
            activity as AppCompatActivity
        ).getString("track_color_key", defaultColor) ?: defaultColor
        
        val polyline = Polyline()
        polyline.outlinePaint.color = try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            Color.parseColor(defaultColor)
        }
        
        val list = points.split("/")
        list.forEach {
            if (it.isNotEmpty()) {
                val latLong = it.split(",")
                polyline.addPoint(GeoPoint(latLong[0].toDouble(), latLong[1].toDouble()))
            }
        }
        
        return polyline
    }

    private fun initOSM(){
        Configuration.getInstance().load(
            activity?.applicationContext,
            activity?.getSharedPreferences(
                OSM_PREFERENCES, Context.MODE_PRIVATE
            ))
    }

    private fun initMap() = with(binding){
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(17.0)
    }

    private fun setMarkers(trackList: List<GeoPoint>) = with(binding){
        val startMarker = Marker(map)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.position = trackList[0]
        startMarker.icon = getDrawable(context as AppCompatActivity, R.drawable.ic_start)
        map.overlays.add(startMarker)
        val finishMarker = Marker(map)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.position = trackList[trackList.size  - 1]
        finishMarker.icon = getDrawable(context as AppCompatActivity, R.drawable.ic_finish)
        map.overlays.add(startMarker)
        map.overlays.add(finishMarker)
    }

    private fun goToLocation(){
        activity?.runOnUiThread{
            mapController.animateTo(initPoint)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }
}