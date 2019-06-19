package ru.breffi.storyiot

import android.app.Application
import android.content.Intent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, ru.breffi.lib.HttpStoryIoTService::class.java)
        startService(intent)
    }
}