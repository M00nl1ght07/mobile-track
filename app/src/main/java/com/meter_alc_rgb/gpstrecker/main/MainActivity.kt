package com.meter_alc_rgb.gpstrecker.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.databinding.ActivityMainBinding
import com.meter_alc_rgb.gpstrecker.fragments.FragmentManager
import com.meter_alc_rgb.gpstrecker.fragments.MainFragment
import com.meter_alc_rgb.gpstrecker.fragments.SettingsFragment
import com.meter_alc_rgb.gpstrecker.fragments.TrackListFragment

/**
 * Главная активность приложения.
 * Управляет навигацией между фрагментами и содержит общую ViewModel.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Объект привязки для доступа к элементам интерфейса
     */
    private lateinit var binding: ActivityMainBinding
    
    /**
     * ViewModel для доступа к данным треков и операций с ними
     */
    val model: MyViewModel by viewModels{
        MyViewModel.MainViewModelFactory((applicationContext as MainApp).database)
    }

    /**
     * Вызывается при создании активности.
     * Инициализирует интерфейс и устанавливает начальный фрагмент.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomMenuListener()
        FragmentManager.setFragment(MainFragment.newInstance(), this)
    }

    /**
     * Настраивает слушатель для нижнего меню навигации.
     * Переключает фрагменты в зависимости от выбранного пункта меню.
     */
    private fun bottomMenuListener(){
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.id_map -> FragmentManager.setFragment(MainFragment.newInstance(), this)
                R.id.id_list -> FragmentManager.setFragment(TrackListFragment.newInstance(), this)
                R.id.id_settings -> FragmentManager.setFragment(SettingsFragment(), this)
            }
            true
        }
    }

    /**
     * Вызывается при уничтожении активности.
     * Очищает ссылку на текущий фрагмент.
     */
    override fun onDestroy() {
        super.onDestroy()
        FragmentManager.currentFragment = null
    }
}