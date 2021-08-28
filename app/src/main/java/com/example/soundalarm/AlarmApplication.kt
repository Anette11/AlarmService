package com.example.soundalarm

import android.app.Application

class AlarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeSharedPreferences()
    }

    private fun initializeSharedPreferences() {
        AlarmSharedPreferences.initializeSharedPreferences(this)
    }
}