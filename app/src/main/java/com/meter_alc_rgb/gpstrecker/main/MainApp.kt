package com.meter_alc_rgb.gpstrecker.main

import android.app.Application
import com.meter_alc_rgb.gpstrecker.database.MainDataBase


class MainApp : Application() {
    val database by lazy { MainDataBase.getDataBase(this) }
}