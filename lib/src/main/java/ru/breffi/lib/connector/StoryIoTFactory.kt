package ru.breffi.lib.connector

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ru.breffi.lib.models.IoTConfig
import ru.breffi.lib.network.Communicator

object StoryIoTFactory {

    fun getSynchrounous(
        context: Context,
        config: IoTConfig,
        appName: String = "",
        appVersion: String = "",
        gson: Gson? = null
    ): SynchronousStoryIoTHttp {
        val converter = gson ?: GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val storyIoTService = Communicator.getStoryIoTService(converter, config.endpoint)
        return SynchronousStoryIoTHttp(
            context,
            converter,
            storyIoTService,
            appName,
            appVersion,
            config
        )
    }

    fun getRx(
        context: Context,
        config: IoTConfig,
        appName: String = "",
        appVersion: String = "",
        gson: Gson? = null
    ): RxStoryIoTHttp {
        val converter = gson ?: GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        val storyIoTService = Communicator.getStoryIoTService(converter, config.endpoint)
        return RxStoryIoTHttp(
            context,
            converter,
            storyIoTService,
            appName,
            appVersion,
            config
        )
    }
}