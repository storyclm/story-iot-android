package ru.breffi.lib

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.breffi.lib.models.StoryMessage
import ru.breffi.lib.network.Communicator
import ru.breffi.lib.network.NetworkUtil
import ru.breffi.lib.network.SmallMessageResponse
import ru.breffi.lib.network.StoryIoTService
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class StoryIoTHttpConnector(var context: Context? = null) {

    private lateinit var okHttpClient: OkHttpClient
    private var gson: Gson = Gson()
    var appName: String = ""
    var appVersion: String = ""
    var smallMessages: ArrayList<StoryMessage>? = null
    lateinit var storyIoTService: StoryIoTService

    data class Builder(var context: Context? = null) {

        private var appName: String? = null
        private var appVersion: String? = null
        var smallMessages: ArrayList<StoryMessage>? = null

        fun setAppName(appName: String) = apply { this.appName = appName }
        fun setAppVersion(appVersion: String) = apply { this.appVersion = appVersion }
        fun setSmallMessages(smallMessages: ArrayList<StoryMessage>?) = apply { this.smallMessages = smallMessages }

        fun build(): StoryIoTHttpConnector {
            val storyIoTHttpConnector = StoryIoTHttpConnector(context)
            appName?.let {
                storyIoTHttpConnector.appName = it
            }
            appVersion?.let {
                storyIoTHttpConnector.appVersion = it
            }
            storyIoTHttpConnector.smallMessages = smallMessages

            return storyIoTHttpConnector
        }
    }

    companion object {
        const val TAG = "StoryIoTHttpConnector"
        val endpoint = "https://staging-iot.storychannels.app"
        val hub = "b47bbc659eb344888f9f92ed3261d8dc"
        val key = "df94b12c3355425eb4efa406f09e8b9f"
        val expiration = "2020-05-28T09:02:49.5754586Z"
    }

    init {
        initHttpClient()
        storyIoTService = Communicator.getStoryIoTService()
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
        val signature = Signature.create(key, expiration)
        return "$endpoint/$hub/publish/?key=$key&expiration=$expiration&signature=$signature"
    }

    fun publishSmallMessages(smallMessages: ArrayList<StoryMessage>?) {
        smallMessages?.let {
            val disposable = sendPost(buildUrl(), it[0])
                .subscribe({ response: Response? ->
                    response?.body()?.let {
                        val smallMessageResponse: SmallMessageResponse =
                            gson.fromJson(it.string(), SmallMessageResponse::class.java)
                        Log.e(TAG, "publishSmallMessage $smallMessageResponse")
                    }
                }, { t -> t.printStackTrace() })
        }
    }

    fun publishSmallMessagesWithRetrofit(storyMessages: ArrayList<StoryMessage>?) {
        storyMessages?.let {
            val disposable = storyIoTService.publishSmallMessage(
                hub,
                key,
                expiration,
                Signature.create(key, expiration),
                buildBody(it[0]),
                buildHeadersMap(it[0])
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ smallMessageResponse: SmallMessageResponse? ->
                    Log.e(TAG, "publishSmallMessage $smallMessageResponse")
                }, { t -> t.printStackTrace() })
        }
    }

    fun publishSmallMessages() {
        smallMessages?.let {
            val disposable = sendPost(buildUrl(), it[0])
                .subscribe({ response: Response? ->
                    response?.body()?.let {
                        val smallMessageResponse: SmallMessageResponse =
                            gson.fromJson(it.string(), SmallMessageResponse::class.java)
                        Log.e(TAG, "publishSmallMessage $smallMessageResponse")
                    }
                }, { t -> t.printStackTrace() })
        }
    }

    fun publishLargeMessages(storyMessages: ArrayList<StoryMessage>?) {
        storyMessages?.let {
            storyIoTService.publishLargeMessageFirst(
                hub,
                key,
                expiration,
                Signature.create(key, expiration),
                buildHeadersMap(it[0])
            )
                .flatMap { messageResponse ->
                    storyIoTService.publishLargeMessageSecond(
                        messageResponse.path,
                        buildLargeBody(it[0].body),
                        buildLargeHeaders(it[0].body)
                    )
                        .flatMap { responseBody ->
                            storyIoTService.confirmLargeMessagePublication(
                                hub,
                                messageResponse.id,
                                key,
                                expiration,
                                Signature.create(key, expiration)
                            )
                        }
                }
//                .flatMap { responseBody -> storyIoTService.confirmLargeMessagePublication(hub,
//                    it[0].id,
//                    key,
//                    expiration,
//                    Signature.create(key, expiration)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Log.e(TAG, "publishLargeMessages $response")
                }, { t -> t.printStackTrace() })
        }
    }

    private fun sendPost(url: String, smallMessage: StoryMessage): Observable<Response> {
        return Observable.create<Response> { emitter ->
            val body = buildBody(smallMessage)
            var builder: Request.Builder = Request.Builder()
                .url(url)
                .post(body)
            builder = setHeaders(builder, smallMessage)
            val request = builder.build()
            performHttpRequest(emitter, request)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun buildBody(smallMessage: StoryMessage): RequestBody {
        val utf = MediaType.parse("text/plain; charset=utf-8")
        return RequestBody.create(utf, gson.toJson(smallMessage.body))
    }


    private fun buildLargeBody(any: Any?): RequestBody {
        val bytes: ByteArray = when (any) {
            is File -> any.readBytes()
            is String -> any.toByteArray()
            else -> ByteArray(0)
        }
        return RequestBody.create(MediaType.parse("application/octet-stream"), bytes)
    }

    private fun buildLargeHeaders(any: Any?) : HashMap<String, String>{
        val bytes: ByteArray = when (any) {
            is File -> any.readBytes()
            is String -> any.toByteArray()
            else -> ByteArray(0)
        }
        var sha512 = ""
        try {
            val digest: MessageDigest = MessageDigest.getInstance("SHA-512")
            val hash: ByteArray = digest.digest(bytes)
            sha512 = Base64.encodeToString(hash, Base64.DEFAULT).replace("\n", "\\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val map = HashMap<String, String>()
        map[StoryHeaders.LARGE_MESSAGE_FILE_HASH] = "base64;sha512;$sha512"
        map[StoryHeaders.LARGE_MESSAGE_BLOB] = "BlockBlob"
        return map
    }

    private fun setHeaders(builder: Request.Builder, smallMessage: StoryMessage): Request.Builder {
        val map = HashMap<String, String>()
        smallMessage.eventId?.let {
            map[StoryHeaders.EID] = it
        }
        smallMessage.userId?.let {
            map[StoryHeaders.UID] = it
        }
        smallMessage.deviceId?.let {
            map[StoryHeaders.DID] = it
        }
        smallMessage.correlationToken?.let {
            map[StoryHeaders.CT] = it
        }
        smallMessage.id?.let {
            map[StoryHeaders.ID] = it
        }
        smallMessage.operationType?.let {
            map[StoryHeaders.CUD] = it
        }
        map[StoryHeaders.M] = Build.MODEL
        map[StoryHeaders.SN] = "need user permission"
        map[StoryHeaders.OS] = "Android"
        map[StoryHeaders.OSV] = Build.VERSION.RELEASE
        map[StoryHeaders.AN] = appName
        map[StoryHeaders.AV] = appVersion
        map[StoryHeaders.IT] = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
            Locale.getDefault()
        ).format(Date())/*"2020-05-28T09:02:49.5754586"*/
        map[StoryHeaders.TZ] = (TimeZone.getDefault().rawOffset / DateUtils.HOUR_IN_MILLIS).toInt().toString()
        map[StoryHeaders.GEO] = "need user permission"/*"on;-34.8799074,174.7565664"*/
        map[StoryHeaders.NS] = NetworkUtil.getConnectivityStatusString(context)
        map[StoryHeaders.LNG] = Locale.getDefault().language
        builder.headers(Headers.of(map))
        return builder
    }

    private fun buildHeadersMap(smallMessage: StoryMessage): HashMap<String, String> {
        val map = HashMap<String, String>()
        smallMessage.eventId?.let {
            map[StoryHeaders.EID] = it
        }
        smallMessage.userId?.let {
            map[StoryHeaders.UID] = it
        }
        smallMessage.deviceId?.let {
            map[StoryHeaders.DID] = it
        }
        smallMessage.correlationToken?.let {
            map[StoryHeaders.CT] = it
        }
        smallMessage.id?.let {
            map[StoryHeaders.ID] = it
        }
        smallMessage.operationType?.let {
            map[StoryHeaders.CUD] = it
        }
        map[StoryHeaders.M] = Build.MODEL
        map[StoryHeaders.SN] = "need user permission"
        map[StoryHeaders.OS] = "Android"
        map[StoryHeaders.OSV] = Build.VERSION.RELEASE
        map[StoryHeaders.AN] = appName
        map[StoryHeaders.AV] = appVersion
        map[StoryHeaders.IT] = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
            Locale.getDefault()
        ).format(Date())/*"2020-05-28T09:02:49.5754586"*/
        map[StoryHeaders.TZ] = (TimeZone.getDefault().rawOffset / DateUtils.HOUR_IN_MILLIS).toInt().toString()
        map[StoryHeaders.GEO] = "need user permission"/*"on;-34.8799074,174.7565664"*/
        map[StoryHeaders.NS] = NetworkUtil.getConnectivityStatusString(context)
        map[StoryHeaders.LNG] = Locale.getDefault().language
        return map
    }

    private fun performHttpRequest(emitter: ObservableEmitter<Response>, request: Request) {
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