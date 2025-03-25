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


/**
 * Фрагмент для просмотра сохраненного трека на карте.
 * Отображает маршрут, начальную и конечную точки, а также статистику трека.
 */
class ViewTrackFragment : BaseFragment("view_fragment") {
    /**
     * Начальная точка трека для центрирования карты
     */
    private var initPoint: GeoPoint? = null
    
    /**
     * Контроллер карты для управления отображением
     */
    private lateinit var mapController: IMapController
    
    /**
     * Объект привязки для доступа к элементам интерфейса
     */
    private lateinit var binding: FragmentViewTrackBinding
    
    /**
     * ViewModel для доступа к данным треков
     */
    private val model: MyViewModel by activityViewModels{
        MyViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
    }

    /**
     * Создает и возвращает представление фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initOSM()
        binding = FragmentViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Вызывается после создания представления фрагмента
     * Инициализирует карту и настраивает обновление данных
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        updateTrack()
        binding.fbMyLocation.setOnClickListener{
            goToLocation()
        }
    }

    /**
     * Вызывается при отсоединении фрагмента от активности
     */
    override fun onDetach() {
        super.onDetach()
    }

    /**
     * Обновляет отображение трека на карте и информацию о нем
     * Наблюдает за изменениями выбранного трека
     */
    private fun updateTrack() = with(binding){
        model.liveDataTrackItem.observe(viewLifecycleOwner){
            val distance = getString(R.string.distance, it.distance)
            val time = getString(R.string.time, it.time)
            val velocity = getString(R.string.velocity, it.velocity)
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

    /**
     * Преобразует строку с координатами в объект Polyline для отображения на карте
     * 
     * @param points Строка с координатами в формате "lat1,lon1/lat2,lon2/..."
     * @return Объект Polyline с настроенным цветом и точками маршрута
     */
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

    /**
     * Инициализирует конфигурацию OSM (OpenStreetMap)
     */
    private fun initOSM(){
        Configuration.getInstance().load(
            activity?.applicationContext,
            activity?.getSharedPreferences(
                OSM_PREFERENCES, Context.MODE_PRIVATE
            ))
    }

    /**
     * Инициализирует карту и настраивает ее параметры
     */
    private fun initMap() = with(binding){
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        mapController.setZoom(17.0)
    }

    /**
     * Устанавливает маркеры начала и конца трека на карте
     * 
     * @param trackList Список точек трека
     */
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

    /**
     * Перемещает карту к начальной точке трека
     */
    private fun goToLocation(){
        activity?.runOnUiThread{
            mapController.animateTo(initPoint)
        }
    }

    companion object {
        /**
         * Создает новый экземпляр фрагмента
         * 
         * @return Новый экземпляр ViewTrackFragment
         */
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }
}