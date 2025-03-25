package com.meter_alc_rgb.gpstrecker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<TrackItem>>
    @Insert
    suspend fun insertTrack(item: TrackItem)
    @Delete
    suspend fun deleteTrack(item: TrackItem)
}