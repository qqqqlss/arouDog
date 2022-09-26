package com.example.aroundog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionSupport(var activity:Activity, var context:Context) {
    private lateinit var permissionList:MutableList<String>
    private var MULTIPLE_PERMISSIONS = 1023//request code

    val permissions = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
//        Manifest.permission.READ_LOGS //있으면 실행 안됨
    )

    fun getPermission():Array<String>{
        return permissions
    }

    //허용되지 않은 권한 체크
    fun check(): Boolean {
        var result:Int
        permissionList= mutableListOf()
        for (per in permissions){
            result = ContextCompat.checkSelfPermission(context, per)
            if(result != PackageManager.PERMISSION_GRANTED)
                //허용되지 않은 퍼미션 추가
                permissionList.add(per)
        }

        if(permissionList.size >0){
            return false
        }
        return true
    }
    companion object{
        fun hasLocationPermissions(context: Context): Boolean {
            return (ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }
    }
}