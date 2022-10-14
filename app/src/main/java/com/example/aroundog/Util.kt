package com.example.aroundog

import android.app.Activity
import androidx.fragment.app.Fragment

object Util {
    fun progressOn(activity: Activity){
        BaseApplication.instance.progressOn(activity)
    }
    fun progressOff(){
        BaseApplication.instance.progressOff()
    }

    fun progressOnInFragment(fragment: Fragment){
        BaseApplication.instance.progressOnInFragment(fragment)
    }
    fun progressOffInFragment(){
        BaseApplication.instance.progressOffInFragment()
    }
}