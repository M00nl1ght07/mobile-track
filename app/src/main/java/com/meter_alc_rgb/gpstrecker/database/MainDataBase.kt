package com.meter_alc_rgb.gpstrecker.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec

/**
 * Основной класс базы данных приложения, использующий Room.
 * Определяет структуру базы данных и предоставляет доступ к DAO.
 */
@Database (entities = [TrackItem::class], version = 1, exportSchema = true)
abstract class MainDataBase : RoomDatabase() {
    /**
     * Возвращает объект DAO для выполнения операций с базой данных.
     * @return Объект Dao для доступа к таблицам базы данных.
     */
    abstract fun getDao(): Dao

    companion object{
        /**
         * Экземпляр базы данных, использующий паттерн Singleton.
         * Аннотация @Volatile гарантирует, что все потоки видят актуальное значение переменной.
         */
        @Volatile
        private var INSTANCE: MainDataBase? = null
        
        /**
         * Получает экземпляр базы данных, создавая его при необходимости.
         * Использует паттерн Singleton для обеспечения единственного экземпляра базы данных.
         * 
         * @param context Контекст приложения для создания базы данных.
         * @return Экземпляр базы данных MainDataBase.
         */
        fun getDataBase(context: Context): MainDataBase{
            return INSTANCE ?: synchronized(this){
                // Блок synchronized предотвращает одновременное создание нескольких экземпляров
                // базы данных разными потоками
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDataBase::class.java,
                    "LucinaTrackGps.db" // Имя файла базы данных
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}