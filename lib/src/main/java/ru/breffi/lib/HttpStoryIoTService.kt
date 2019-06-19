package ru.breffi.lib

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.breffi.lib.database.StoryIoTRealmModule

class HttpStoryIoTService : Service() {

    companion object{
        const val TAG = "HttpStoryIoTService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        initRealm()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()
    }

    private fun initRealm() {
            Realm.init(this)
            val realmConfiguration = RealmConfiguration.Builder()
                .schemaVersion(1)
                .name("storyiot.realm")
                .modules(StoryIoTRealmModule())
                .deleteRealmIfMigrationNeeded()
                .build()
            Realm.compactRealm(realmConfiguration)
            Realm.setDefaultConfiguration(realmConfiguration)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(TAG, "onBind")
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
    }
}
