package com.example.aroundog

import android.app.Application
import android.os.Environment
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class LogSaver: Application() {
    override fun onCreate() {
        super.onCreate()
        var date:Date = Date(System.currentTimeMillis())
        var format:SimpleDateFormat = SimpleDateFormat("yyMMddhhss")
        var time:String = format.format(date)

        var downDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var path = downDirectory.path
        var logFile:File = File(path, time+".txt")
        try {
            var process:Process = Runtime.getRuntime().exec("logcat -c")
            process = Runtime.getRuntime().exec("logcat -f " + logFile)
        }catch (e:Exception){

        }
    }
}