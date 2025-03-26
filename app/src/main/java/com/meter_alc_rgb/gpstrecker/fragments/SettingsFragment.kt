package com.meter_alc_rgb.gpstrecker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.slider.Slider
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.fragments.MainFragment

/**
 * Фрагмент настроек приложения.
 * Отвечает за отображение и обработку пользовательских настроек, таких как
 * частота обновления местоположения и цвет трека на карте.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    /**
     * Preference для настройки времени обновления местоположения
     */
    private lateinit var locTime: Preference
    
    /**
     * Preference для настройки цвета трека на карте
     */
    private lateinit var trackColor: Preference
    
    /**
     * Вызывается при создании фрагмента настроек.
     * Загружает настройки из XML-ресурса и инициализирует компоненты.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
          setPreferencesFromResource(R.xml.main_preferences, rootKey)
        init()
        setLocPrefInitValues()
        setTRackColorPrefInitValues()
    }

    /**
     * Инициализирует элементы настроек и устанавливает слушатели изменений.
     */
    private fun init(){
        locTime = findPreference("update_time_key")!!
        trackColor = findPreference("track_color_key")!!
        locTime.onPreferenceChangeListener = onChangeTimeListener()
        trackColor.onPreferenceChangeListener = onChangeTrackColorListener()
    }

    /**
     * Создает слушатель изменений для настройки времени обновления местоположения.
     * Обновляет заголовок настройки при изменении значения.
     * 
     * @return Слушатель изменений для Preference
     */
    private fun onChangeTimeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener{
                pref, value ->
            val nameArray = resources.getStringArray(R.array.update_loc_name)
            val valueArray = resources.getStringArray(R.array.update_loc_value)
            val title = pref.title.toString().substringBefore(":")
            pref.title = "$title: ${nameArray[valueArray.indexOf(value.toString())]}"
            true
        }
    }

    /**
     * Создает слушатель изменений для настройки цвета трека.
     * Обновляет цвет иконки при изменении значения.
     * 
     * @return Слушатель изменений для Preference
     */
    private fun onChangeTrackColorListener(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { pref, value ->
            try {
                // Меняем цвет иконки настройки
                val colorValue = value.toString()
                val color = Color.parseColor(colorValue)
                pref.icon?.let { changeDrawableColor(it, color) }
                
                // Принудительно применяем изменение сразу ко всем фрагментам
                val fragmentManager = requireActivity().supportFragmentManager
                for (fragment in fragmentManager.fragments) {
                    if (fragment is MainFragment) {
                        fragment.updateTrackColorFromPreferences()
                        break
                    }
                }
            } catch (e: Exception) {
                // Обрабатываем ошибки парсинга цвета
            }
            true
        }
    }

    /**
     * Устанавливает начальные значения для настройки времени обновления местоположения.
     * Обновляет заголовок настройки в соответствии с сохраненным значением.
     */
    private fun setLocPrefInitValues() {
        val pref = locTime.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.update_loc_name)
        val valueArray = resources.getStringArray(R.array.update_loc_value)
        val title = locTime.title.toString().substringBefore(":")
        val time = pref?.getString("update_time_key", valueArray[0])
        val index = valueArray.indexOf(time)
        
        if (index >= 0) {
            locTime.title = "$title: ${nameArray[index]}"
        } else {
            locTime.title = "$title: ${nameArray[0]}"
        }
    }

    /**
     * Устанавливает начальные значения для настройки цвета трека.
     * Обновляет цвет иконки в соответствии с сохраненным значением.
     * Обрабатывает возможные ошибки при парсинге цвета.
     */
    private fun setTRackColorPrefInitValues() {
        val pref = trackColor.preferenceManager.sharedPreferences
        val valueArray = resources.getStringArray(R.array.track_color_values)
        val color = pref?.getString("track_color_key", valueArray[0]) ?: valueArray[0]
        
        try {
            trackColor.icon?.let { changeDrawableColor(it, Color.parseColor(color)) }
        } catch (e: IllegalArgumentException) {
            trackColor.icon?.let { changeDrawableColor(it, Color.parseColor(valueArray[0])) }
        }
    }
}