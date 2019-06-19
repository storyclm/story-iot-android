package ru.breffi.lib

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class StoryIoTHttpConnector(var context: Context? = null) {

    private lateinit var okHttpClient: OkHttpClient

    data class Builder(var context: Context? = null) {
        fun context(context: Context) = apply { this.context = context }
        fun build(): StoryIoTHttpConnector {
            return StoryIoTHttpConnector(context)
        }
    }

    init {
        initHttpClient()
    }

    private fun initHttpClient() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
            .build()
    }
}