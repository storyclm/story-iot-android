package ru.breffi.lib

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class StoryIoTHttpConnector(var context: Context? = null) {

    private lateinit var okHttpClient: OkHttpClient

    data class Builder(var context: Context? = null) {
        fun build(): StoryIoTHttpConnector {
            return StoryIoTHttpConnector(context)
        }
    }

    companion object {
        const val TAG = "StoryIoTHttpConnector"
    }

    init {
        initHttpClient()
        publishMessage(buildUrl())
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

    private fun buildUrl(): String {
        val endpoint = "https://staging-iot.storychannels.app"
        val hub = "b47bbc659eb344888f9f92ed3261d8dc"
        val key = "df94b12c3355425eb4efa406f09e8b9f"
        val expiration = "2020-05-28T09:02:49.5754586Z"
        val signature = buildSignature(key, expiration)
        return "$endpoint/$hub/feed/?key=$key&expiration=$expiration&signature=$signature"
    }

    private fun buildSignature(key: String, expiration: String): String? {
        val secret = "163af6783ae14d5f829288d1ca44950e"
        val message = "key=${key}expiration=$expiration"
        Log.e(TAG, "message = $message")
        val shaHMAC = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA512")
        shaHMAC.init(secretKey)
        var hash = Base64.encodeToString(shaHMAC.doFinal(message.toByteArray()), Base64.DEFAULT)
        Log.e(TAG, "base64 = $hash")
        hash = hash.replace("/", "_")
            .replace("+", "-")
        Log.e(TAG, "base64 with replace = $hash")
        return hash
    }

    private fun publishMessage(url: String) {
        performPost(url)
    }

    private fun performPost(url: String) {
        val disposable = Observable.create<Any> { emitter ->
            val body = getRequestBody("Body")
            var builder: Request.Builder = Request.Builder()
                .url(url)
                .post(body)
            builder = setHeaders(builder, null)
            val request = builder.build()
            performHttpRequest(emitter, request)
        }.subscribe({ any: Any? -> Log.e(TAG, "performPost $any") }, { t -> t.printStackTrace() })
    }

    private fun getRequestBody(body: String): RequestBody {
        val utf = MediaType.parse("text; charset=utf-8")
        var bodyStr = ""
        if (!TextUtils.isEmpty(body)) {
            val base64 = Base64.decode(body, Base64.DEFAULT)
            bodyStr = String(base64)
        }
        return RequestBody.create(utf, bodyStr)
    }

    private fun setHeaders(builder: Request.Builder, headers: List<String>?): Request.Builder {
        val map = HashMap<String, String>()
        builder.headers(Headers.of(map))
        return builder
    }

    private fun performHttpRequest(emitter: ObservableEmitter<Any>, request: Request) {
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                emitter.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                emitter.onNext(response)
            }
        })
    }
}