package com.meter_alc_rgb.gpstrecker.fragments

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.meter_alc_rgb.gpstrecker.R

object FragmentManager {
    var currentFragment: BaseFragment? = null

    fun setFragment(newFragment: Fragment, act: AppCompatActivity){
        currentFragment = if (newFragment is BaseFragment){
            if((currentFragment?.name ?: "empty") == newFragment.name) return
            newFragment
        } else {
            null
        }
        val transaction = act.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        transaction.replace(R.id.placeHolder, newFragment)
        transaction.commit()
    }
}