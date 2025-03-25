package com.meter_alc_rgb.gpstrecker.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.meter_alc_rgb.gpstrecker.*
import com.meter_alc_rgb.gpstrecker.database.TrackItem
import com.meter_alc_rgb.gpstrecker.databinding.FragmentMainBinding
import com.meter_alc_rgb.gpstrecker.main.MainApp
import com.meter_alc_rgb.gpstrecker.main.MyViewModel
import com.meter_alc_rgb.gpstrecker.utils.DialogManager
import com.meter_alc_rgb.gpstrecker.utils.LocationModel
import com.meter_alc_rgb.gpstrecker.utils.LocationService
import com.meter_alc_rgb.gpstrecker.utils.LocationUtils
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.collections.ArrayList

const val OSM_PREFERENCES = "osm_prefs"
const val INIT_ZOOM = 20.0
class MainFragment : BaseFragment("main") {
    private var lastTrackItem: TrackItem? = null
    private var timer: Timer? = null
    private lateinit var mapController: IMapController
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    private var startTime = 0L
    private var isServiceRunning = false
    private var lineColor = Color.BLUE
    private val model: MyViewModel by activityViewModels{
        MyViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initOSM()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionsListener()
        checkPermissions()
        initButtons()
        initLocationUpdates()
        timeUpdate()
        registerFilter()
        checkServiceState()
        checkLocationEnabled()
        Log.d("MyLog","Is service running: ${LocationService.isRunning}")
    }

    private fun registerFilter(){
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, IntentFilter(LocationService.GPS_LOCATION_INTENT))
    }

    private fun initLocationUpdates() = with(binding) {
        model.liveDataModel.observe(viewLifecycleOwner) {

            val distance = "Расстояние: ${String.format("%.1f", it.distance)} м"
            val velocity = "Скорость: ${String.format("%.1f", 3.6 * it.velocity)} км/ч"
            val mVelocity = "Средняя скорость: ${getMiddleVelocity(it.distance)} км/ч"
            tvVelocity.text = velocity
            tvDistance.text = distance
            tvMiddleVelocity.text = mVelocity
            lastTrackItem = TrackItem(
                null,
                getCurrentTime(),
                LocationUtils.getCurrentDate(),
                String.format("%.1f", it.distance / 1000),
                getMiddleVelocity(it.distance),
                getTrackPoints(it.polyline)
            )
        }
    }

    private fun getTrackPoints(list: List<GeoPoint>): String{
         val sBuilder = StringBuilder()
        list.forEach {
            sBuilder.append("${it.latitude},${it.longitude}/")
        }
         return sBuilder.toString()
    }

    private fun timeUpdate(){
        model.liveDataTimeCounter.observe(viewLifecycleOwner){
            binding.tvStartTime.text = it
        }
    }

    private fun initButtons() = with(binding){
        val onClick = onClick()
        fbStartStop.setOnClickListener(onClick)
        fbMyLocation.setOnClickListener(onClick)
    }

    private fun onClick(): View.OnClickListener{
        return View.OnClickListener {
            when(it.id){
                R.id.fbStartStop -> onClickStartStop()
                R.id.fbMyLocation -> goToMyLocation()
            }
        }
    }

    private fun onClickStartStop() {
        if (!isServiceRunning) {
            startTimer()
            startLocService()
        } else {
            stopLocationService()
        }
        isServiceRunning = !isServiceRunning
    }

    private fun startLocService(){
        val i = Intent(context, LocationService::class.java).apply {
            putExtra("start_time", startTime)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(i)
        } else {
            activity?.startService(i)
        }
        binding.fbStartStop.setImageResource(R.drawable.ic_stop_track)
    }

    private fun stopLocationService() = with(binding){
        val i = Intent(context, LocationService::class.java)
        activity?.stopService(i)
        fbStartStop.setImageResource(R.drawable.ic_start_track)
        stopTimer()
        showSaveDialog()
    }

    private fun showSaveDialog() {
        lastTrackItem?.let {
            DialogManager.showSaveTrackDialog(context as AppCompatActivity, it,
                object : DialogManager.Listener {
                    override fun onClick() {
                        model.insertTrack(it)
                        model.liveDataModel.value = LocationModel(polyline = ArrayList())
                    }
                })
        }
    }

    private fun stopTimer(){
        timer?.cancel()
        model.liveDataTimeCounter.value = "Время пути: 00:00:00 ч"
    }

    private fun checkServiceState(){
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fbStartStop.setImageResource(R.drawable.ic_stop_track)
            startTimer()
        }
    }

    override fun onDetach() {
        super.onDetach()
        timer?.cancel()
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationService.GPS_LOCATION_INTENT) {
                val dataModel = intent
                    .getSerializableExtra(
                        LocationService.LOCATION_INTENT
                    ) as LocationModel
                activity?.runOnUiThread {
                    model.liveDataModel.value = dataModel
                    binding.map.overlays.add(getPolylineFromList(dataModel.polyline))
                }
            }
        }
    }

    private fun getPolylineFromList(list: List<GeoPoint>): Polyline{
        val polyline = Polyline()
        polyline.outlinePaint.color = lineColor
        list.forEach {
            polyline.addPoint(it)
        }
        return polyline
    }

    private fun startTimer() = with(binding){
        timer?.cancel()
        Log.d("MyLog","Start time: $startTime")
        startTime = if(LocationService.startTime == 0L)
            System.currentTimeMillis()
        else LocationService.startTime
        timer = Timer("Время в пути")
        timer?.schedule(object : TimerTask(){
            override fun run() {
                activity?.runOnUiThread {
                    model.liveDataTimeCounter.value = "Время: ${getCurrentTime()}"
                }
            }

        },1000, 1000)
    }

    private fun getMiddleVelocity(distance: Float): String{
        return String.format("%.1f", 3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f)))
    }

    private fun getCurrentTime(): String{
        return LocationUtils.getTime(System.currentTimeMillis() - startTime)
    }

    // Permissions
    private fun checkPermissions(){
        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            } else {
                pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        } else {
            initMap()
        }
    }

    private fun permissionsListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                showToast("Разрешение получено!")
                initMap()
            } else {
                showToast("Нет разрешения!")
            }
        }
    }

    private fun checkLocationEnabled(){
        val m = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = m.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled){
            DialogManager.showLocationEnableDialog(activity as AppCompatActivity,
                object : DialogManager.Listener{
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                })
        }
    }

    // OSM init
    private fun initOSM(){
        Configuration.getInstance().load(
            activity?.applicationContext,
            activity?.getSharedPreferences(
                OSM_PREFERENCES, Context.MODE_PRIVATE
            ))
    }

    private fun initMap() = with(binding){
        val defaultColor = "#0017EF"
        val colorStr = PreferenceManager.getDefaultSharedPreferences(
            activity as AppCompatActivity
        ).getString("track_color_key", defaultColor) ?: defaultColor
        
        lineColor = try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            Color.parseColor(defaultColor)
        }
        
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(INIT_ZOOM)
        val mGpsMyLocationProvider = GpsMyLocationProvider(activity)
        mLocationOverlay = MyLocationNewOverlay(mGpsMyLocationProvider, map)
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.enableFollowLocation()
        mLocationOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocationOverlay)
            goToMyLocation()
        }
    }

    private fun goToMyLocation(){
        activity?.runOnUiThread{
            mapController.animateTo(mLocationOverlay.myLocation)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}