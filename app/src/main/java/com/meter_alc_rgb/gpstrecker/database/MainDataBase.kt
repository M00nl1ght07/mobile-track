package com.meter_alc_rgb.gpstrecker.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec

@Database (entities = [TrackItem::class], version = 1, exportSchema = true)
abstract class MainDataBase : RoomDatabase() {
    abstract fun getDao(): Dao

    companion object{
        @Volatile
        private var INSTANCE: MainDataBase? = null
        fun getDataBase(context: Context): MainDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDataBase::class.java,
                    "LucinaTrackGps.db"
                ).build()
                instance
            }
        }
    }
}