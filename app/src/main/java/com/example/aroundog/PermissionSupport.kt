package com.example.aroundog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionSupport(_activity:Activity, _context:Context) {
    private var activity:Activity = _activity
    private var context:Context = _context
    private lateinit var permissionList:MutableList<String>
    private var MULTIPLE_PERMISSIONS = 1023

    val permissions = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_LOGS

    )

    fun check(): Boolean {
        var result:Int
        permissionList= mutableListOf()
        for (per in permissions){
            result = ContextCompat.checkSelfPermission(context, per)
            if(result != PackageManager.PERMISSION_GRANTED)
                //허용된 퍼미션 추가
                permissionList.add(per)
        }

        if(!permissionList.isEmpty()){
            return false
        }
        return true
    }
    fun requestPermission(){
        ActivityCompat.requestPermissions(activity,
            permissionList.toTypedArray(), MULTIPLE_PERMISSIONS)
    }

    fun permissionResult(requestCode:Int, permissions: Array<out String>, grantResults:IntArray):Boolean {
        if(requestCode == MULTIPLE_PERMISSIONS && (grantResults.size > 0)){
            for (i in 0..grantResults.size){
                //grantResults 가 0이면 사용자가 허용한 것이고 / -1이면 거부한 것
                // -1이 있는지 체크하여 하나라도 -1이 나온다면 false를 리턴
                if(grantResults[i] == -1){
                    return false
                }
            }
        }
        return true
    }
}