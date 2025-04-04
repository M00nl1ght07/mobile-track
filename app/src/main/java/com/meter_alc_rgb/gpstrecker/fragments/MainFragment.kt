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

/**
 * Константа для имени файла настроек OSM
 */
const val OSM_PREFERENCES = "osm_prefs"

/**
 * Начальный уровень масштабирования карты
 */
const val INIT_ZOOM = 15.0

/**
 * Главный фрагмент приложения, отображающий карту и элементы управления треком.
 * Позволяет пользователю начать/остановить запись трека, отслеживать местоположение
 * и просматривать статистику движения.
 */
class MainFragment : BaseFragment("main") {
    /**
     * Последний записанный трек для сохранения
     */
    private var lastTrackItem: TrackItem? = null
    
    /**
     * Таймер для отсчета времени движения
     */
    private var timer: Timer? = null
    
    /**
     * Контроллер карты для управления отображением
     */
    private lateinit var mapController: IMapController
    
    /**
     * Оверлей для отображения текущего местоположения на карте
     */
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    
    /**
     * Лаунчер для запроса разрешений
     */
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    
    /**
     * Объект привязки для доступа к элементам UI
     */
    private lateinit var binding: FragmentMainBinding
    
    /**
     * Время начала записи трека в миллисекундах
     */
    private var startTime = 0L
    
    /**
     * Флаг, указывающий запущен ли сервис отслеживания местоположения
     */
    private var isServiceRunning = false
    
    /**
     * Цвет линии трека на карте
     */
    private var lineColor = Color.parseColor("#0017EF")
    
    /**
     * Полилиния для отображения трека на карте
     */
    private var polyline: Polyline? = null
    
    /**
     * ViewModel для доступа к данным треков и операций с ними
     */
    private val model: MyViewModel by activityViewModels{
        MyViewModel.MainViewModelFactory((requireContext().applicationContext as MainApp).database)
    }
    
    /**
     * Создает представление фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    /**
     * Вызывается после создания представления фрагмента
     * Инициализирует компоненты и настраивает слушатели
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissionListener()
        checkPermission()
        setOnClicks()
        initLocationUpdates()
        timeUpdate()
        registerFilter()
        checkServiceState()
        checkLocationEnabled()
        initOSM()
        initMap()
    }
    
    /**
     * Вызывается при возобновлении фрагмента.
     * Обновляет настройки, включая цвет трека.
     */
    override fun onResume() {
        super.onResume()
        updateTrackColorFromPreferences()
    }
    
    /**
     * Обновляет цвет трека из настроек приложения.
     */
    fun updateTrackColorFromPreferences() {
        val defaultColor = "#0017EF"
        val colorStr = PreferenceManager.getDefaultSharedPreferences(
            activity as AppCompatActivity
        ).getString("track_color_key", defaultColor) ?: defaultColor
        
        val newColor = try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            Color.parseColor(defaultColor)
        }
        
        // Обновляем цвет только если он изменился
        if (lineColor != newColor) {
            lineColor = newColor
            
            // Обновляем цвет существующей полилинии, если она уже создана
            polyline?.let {
                it.outlinePaint.color = lineColor
                binding.map.invalidate() // Перерисовываем карту
            }
            
            // Обновляем цвета всех полилиний на карте
            for (overlay in binding.map.overlays) {
                if (overlay is Polyline) {
                    overlay.outlinePaint.color = lineColor
                }
            }
            binding.map.invalidate()
        }
    }
    
    /**
     * Обновляет полилинию на карте, добавляя новую точку
     */
    private fun updatePolyline(geoPoint: GeoPoint) {
        if (polyline == null) {
            polyline = Polyline().apply {
                outlinePaint.color = lineColor // Используем текущий цвет
                outlinePaint.strokeWidth = 5f
            }
            binding.map.overlays.add(polyline)
        }
        polyline?.addPoint(geoPoint)
        binding.map.invalidate()
    }
    
    /**
     * Настраивает слушатель результатов запроса разрешений
     */
    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initMap()
                checkLocationEnabled()
            } else {
                showToast(R.string.permission_denied)
            }
        }
    }
    
    /**
     * Проверяет наличие необходимых разрешений и запрашивает их при необходимости
     */
    private fun checkPermission() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // На Android 11 (API 30) и выше разрешение на фоновую локацию нужно запрашивать отдельно
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            // Проверяем необходимость запроса фонового разрешения на Android 11+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // На Android 11+ запрашиваем фоновую локацию отдельно
                    if (!checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        requestBackgroundLocationPermission()
                    }
                } else {
                    // На Android 10 запрашиваем оба разрешения вместе
                    if (!checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        pLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ))
                    }
                }
            }
            initMap()
            checkLocationEnabled()
        }
    }
    
    /**
     * Запрашивает разрешение на фоновую локацию на Android 11+
     * Показывает диалог с объяснением перед запросом разрешения
     */
    private fun requestBackgroundLocationPermission() {
        DialogManager.showBackgroundLocationDialog(activity as AppCompatActivity,
            object : DialogManager.Listener {
                override fun onClick() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    }
                }
            })
    }
    
    /**
     * Регистрирует приемник широковещательных сообщений для получения обновлений местоположения
     */
    private fun registerFilter(){
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, IntentFilter(LocationService.GPS_LOCATION_INTENT))
    }

    /**
     * Инициализирует наблюдение за обновлениями местоположения и обновляет UI соответственно
     */
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

    /**
     * Преобразует список точек GeoPoint в строку для сохранения в базе данных
     * 
     * @param list Список географических точек трека
     * @return Строка с координатами в формате "lat,lon/lat,lon/..."
     */
    private fun getTrackPoints(list: List<GeoPoint>): String{
         val sBuilder = StringBuilder()
        list.forEach {
            sBuilder.append("${it.latitude},${it.longitude}/")
        }
         return sBuilder.toString()
    }

    /**
     * Настраивает наблюдение за обновлениями времени движения
     */
    private fun timeUpdate(){
        model.liveDataTimeCounter.observe(viewLifecycleOwner){
            binding.tvStartTime.text = it
        }
    }

    /**
     * Инициализирует кнопки управления
     */
    private fun setOnClicks() = with(binding){
        val onClick = onClick()
        fbStartStop.setOnClickListener(onClick)
        fbMyLocation.setOnClickListener(onClick)
    }

    /**
     * Создает обработчик нажатий для кнопок
     * 
     * @return Объект слушателя нажатий
     */
    private fun onClick(): View.OnClickListener{
        return View.OnClickListener {
            when(it.id){
                R.id.fbStartStop -> onClickStartStop()
                R.id.fbMyLocation -> goToMyLocation()
            }
        }
    }

    /**
     * Обрабатывает нажатие на кнопку старт/стоп
     * Запускает или останавливает отслеживание местоположения
     */
    private fun onClickStartStop() {
        if (!isServiceRunning) {
            startTimer()
            startLocService()
        } else {
            stopLocationService()
        }
        isServiceRunning = !isServiceRunning
    }

    /**
     * Запускает сервис отслеживания местоположения
     */
    private fun startLocService(){
        val i = Intent(context, LocationService::class.java).apply {
            putExtra("start_time", startTime)
        }
        
        // На Android 12+ (API 31+) нужно использовать другой метод запуска foreground сервиса
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity?.startForegroundService(i)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(i)
        } else {
            activity?.startService(i)
        }
        binding.fbStartStop.setImageResource(R.drawable.ic_stop_track)
    }

    /**
     * Останавливает сервис отслеживания местоположения и предлагает сохранить трек
     */
    private fun stopLocationService() = with(binding){
        val i = Intent(context, LocationService::class.java)
        activity?.stopService(i)
        fbStartStop.setImageResource(R.drawable.ic_start_track)
        stopTimer()
        showSaveDialog()
    }

    /**
     * Отображает диалог для сохранения записанного трека
     */
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

    /**
     * Останавливает таймер отсчета времени движения
     */
    private fun stopTimer(){
        timer?.cancel()
        model.liveDataTimeCounter.value = "Время пути: 00:00:00 ч"
    }

    /**
     * Проверяет текущее состояние сервиса отслеживания и обновляет UI соответственно
     */
    private fun checkServiceState(){
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fbStartStop.setImageResource(R.drawable.ic_stop_track)
            startTimer()
        }
    }

    /**
     * Вызывается при отсоединении фрагмента от активности.
     * Освобождает ресурсы и отменяет регистрацию приемника.
     */
    override fun onDetach() {
        super.onDetach()
        timer?.cancel()
        LocalBroadcastManager
            .getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    /**
     * Приемник широковещательных сообщений для получения обновлений местоположения от сервиса
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationService.GPS_LOCATION_INTENT) {
                val dataModel = intent
                    .getSerializableExtra(
                        LocationService.LOCATION_INTENT
                    ) as LocationModel
                activity?.runOnUiThread {
                    model.liveDataModel.value = dataModel
                    // Обновляем текущий цвет из настроек перед добавлением новой полилинии
                    updateTrackColorFromPreferences()
                    binding.map.overlays.add(getPolylineFromList(dataModel.polyline))
                }
            }
        }
    }

    /**
     * Создает полилинию из списка географических точек для отображения на карте
     * 
     * @param list Список точек GeoPoint
     * @return Объект Polyline для добавления на карту
     */
    private fun getPolylineFromList(list: List<GeoPoint>): Polyline{
        val polyline = Polyline()
        polyline.outlinePaint.color = lineColor
        polyline.outlinePaint.strokeWidth = 5f
        list.forEach {
            polyline.addPoint(it)
        }
        return polyline
    }

    /**
     * Запускает таймер для отсчета времени движения
     */
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

    /**
     * Вычисляет среднюю скорость движения
     * 
     * @param distance Пройденное расстояние в метрах
     * @return Строка со средней скоростью в км/ч
     */
    private fun getMiddleVelocity(distance: Float): String{
        return String.format("%.1f", 3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f)))
    }

    /**
     * Возвращает текущее время движения в формате "ЧЧ:ММ:СС"
     * 
     * @return Строка с форматированным временем
     */
    private fun getCurrentTime(): String{
        return LocationUtils.getTime(System.currentTimeMillis() - startTime)
    }

    /**
     * Проверяет, включены ли службы геолокации на устройстве
     * и предлагает включить их, если они отключены
     */
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

    /**
     * Инициализирует библиотеку OSM (OpenStreetMap)
     */
    private fun initOSM(){
        // Настройка для поддержки Android 10+ (API 29+)
        val ctx = activity?.applicationContext
        Configuration.getInstance().load(
            ctx,
            ctx?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        )
        
        // Установка userAgent для избежания проблем с серверами OSM
        activity?.applicationContext?.packageName?.let {
            Configuration.getInstance().userAgentValue = it
        }
    }

    /**
     * Инициализирует карту и настраивает ее параметры
     */
    private fun initMap() = with(binding){
        updateTrackColorFromPreferences()
        
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

    /**
     * Перемещает карту к текущему местоположению пользователя
     */
    private fun goToMyLocation(){
        activity?.runOnUiThread{
            mapController.animateTo(mLocationOverlay.myLocation)
        }
    }

    companion object {
        /**
         * Создает новый экземпляр MainFragment
         * 
         * @return Новый экземпляр фрагмента
         */
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}