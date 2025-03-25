package com.meter_alc_rgb.gpstrecker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность для хранения информации о маршруте в базе данных.
 * Представляет собой таблицу "track" в базе данных Room.
 */
@Entity (tableName = "track")
data class TrackItem(
    /**
     * Уникальный идентификатор маршрута, генерируется автоматически.
     */
    @PrimaryKey (autoGenerate = true)
    val id: Int?,
    
    /**
     * Время прохождения маршрута в формате строки.
     */
    @ColumnInfo (name = "time")
    val time: String,
    
    /**
     * Дата записи маршрута.
     */
    @ColumnInfo (name = "date")
    val date: String,
    
    /**
     * Пройденное расстояние в километрах.
     */
    @ColumnInfo (name = "distance")
    val distance: String,
    
    /**
     * Средняя скорость движения в км/ч.
     */
    @ColumnInfo (name = "velocity")
    val velocity: String,
    
    /**
     * Строка с сериализованными географическими точками маршрута.
     * Хранит последовательность координат GPS в формате JSON или другом текстовом формате.
     */
    @ColumnInfo (name = "geo_points")
    val geoPoints: String
    )