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


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val model: MyViewModel by viewModels{
        MyViewModel.MainViewModelFactory((applicationContext as MainApp).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomMenuListener()
        FragmentManager.setFragment(MainFragment.newInstance(), this)


    }

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

    override fun onDestroy() {
        super.onDestroy()
        FragmentManager.currentFragment = null
    }
}