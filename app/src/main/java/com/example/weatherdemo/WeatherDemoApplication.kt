package com.example.weatherdemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class WeatherDemoApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN="输入申请的令牌"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }


}