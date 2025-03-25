package com.meter_alc_rgb.gpstrecker.main


import androidx.lifecycle.*
import com.meter_alc_rgb.gpstrecker.database.MainDataBase
import com.meter_alc_rgb.gpstrecker.database.TrackItem
import com.meter_alc_rgb.gpstrecker.utils.LocationModel
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MyViewModel(database: MainDataBase) : ViewModel() {
    private val dao = database.getDao()
    val liveDataModel = MutableLiveData<LocationModel>()
    val liveDataTimeCounter = MutableLiveData<String>()
    val liveDataTrackItem = MutableLiveData<TrackItem>()
    val liveDataTracks: LiveData<List<TrackItem>> = dao.getAllTracks().asLiveData()

    fun insertTrack(item: TrackItem) = viewModelScope.launch {
        dao.insertTrack(item)
    }

    fun deleteTrack(item: TrackItem) = viewModelScope.launch {
        dao.deleteTrack(item)
    }

    class MainViewModelFactory(val database: MainDataBase) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(MyViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return MyViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    }
}