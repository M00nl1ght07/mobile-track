package com.meter_alc_rgb.gpstrecker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс доступа к данным (DAO) для работы с таблицей маршрутов в базе данных.
 * Предоставляет методы для выполнения основных операций CRUD.
 */
@Dao
interface Dao {
    /**
     * Получает все сохраненные маршруты из базы данных.
     * @return Flow-коллекцию всех маршрутов, которая автоматически обновляется при изменении данных.
     */
    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<TrackItem>>
    
    /**
     * Добавляет новый маршрут в базу данных.
     * @param item Объект маршрута для сохранения.
     */
    @Insert
    suspend fun insertTrack(item: TrackItem)
    
    /**
     * Удаляет указанный маршрут из базы данных.
     * @param item Объект маршрута для удаления.
     */
    @Delete
    suspend fun deleteTrack(item: TrackItem)
}