package com.meter_alc_rgb.gpstrecker.main


import androidx.lifecycle.*
import com.meter_alc_rgb.gpstrecker.database.MainDataBase
import com.meter_alc_rgb.gpstrecker.database.TrackItem
import com.meter_alc_rgb.gpstrecker.utils.LocationModel
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

/**
 * ViewModel для управления данными треков и их отображения.
 * Обеспечивает связь между UI и базой данных.
 */
class MyViewModel(database: MainDataBase) : ViewModel() {
    /**
     * Объект доступа к данным для выполнения операций с базой данных
     */
    private val dao = database.getDao()
    
    /**
     * LiveData для текущей модели местоположения
     */
    val liveDataModel = MutableLiveData<LocationModel>()
    
    /**
     * LiveData для отображения времени трекинга
     */
    val liveDataTimeCounter = MutableLiveData<String>()
    
    /**
     * LiveData для текущего элемента трека
     */
    val liveDataTrackItem = MutableLiveData<TrackItem>()
    
    /**
     * LiveData со списком всех треков из базы данных
     */
    val liveDataTracks: LiveData<List<TrackItem>> = dao.getAllTracks().asLiveData()

    /**
     * Вставляет новый трек в базу данных
     * 
     * @param item Элемент трека для вставки
     */
    fun insertTrack(item: TrackItem) = viewModelScope.launch {
        dao.insertTrack(item)
    }

    /**
     * Удаляет трек из базы данных
     * 
     * @param item Элемент трека для удаления
     */
    fun deleteTrack(item: TrackItem) = viewModelScope.launch {
        dao.deleteTrack(item)
    }

    /**
     * Фабрика для создания экземпляров MyViewModel с передачей базы данных
     */
    class MainViewModelFactory(val database: MainDataBase) : ViewModelProvider.Factory{
        /**
         * Создает новый экземпляр ViewModel
         * 
         * @param modelClass Класс ViewModel для создания
         * @return Созданный экземпляр ViewModel
         * @throws IllegalArgumentException если запрошенный класс не является MyViewModel
         */
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(MyViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return MyViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    }
}