package com.meter_alc_rgb.gpstrecker.main

import android.app.Application
import com.meter_alc_rgb.gpstrecker.database.MainDataBase

/**
 * Основной класс приложения, расширяющий Application.
 * Инициализирует и предоставляет доступ к базе данных для всего приложения.
 */
class MainApp : Application() {
    /**
     * Экземпляр базы данных, инициализируемый лениво при первом обращении.
     * Используется для хранения и управления треками и связанными данными.
     */
    val database by lazy { MainDataBase.getDataBase(this) }
}