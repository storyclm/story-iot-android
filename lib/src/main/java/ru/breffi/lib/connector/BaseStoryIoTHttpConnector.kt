package ru.breffi.lib.connector

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.internal.bind.util.ISO8601Utils
import okhttp3.MediaType
import okhttp3.RequestBody
import ru.breffi.lib.SignatureBuilder
import ru.breffi.lib.StoryHeaders
import ru.breffi.lib.models.IoTConfig
import ru.breffi.lib.models.MessageData
import ru.breffi.lib.network.NetworkUtil
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

open class BaseStoryIoTHttpConnector internal constructor(
    private val context: Context,
    private val gson: Gson,
    private val appName: String,
    private val appVersion: String,
    private val config: IoTConfig
) {
    companion object {
        const val TAG = "BaseStoryIoTHttpConnector"
    }

    protected fun getExpiration() : String {
        return ISO8601Utils
            .format(Date(System.currentTimeMillis() + config.expirationInSeconds * 1000))
    }

    protected fun buildSignature(key: String, expiration: String) : String {
        return SignatureBuilder(config.privateKey)
            .addParam("key", key)
            .addParam("expiration", expiration)
            .build()
    }

    protected fun buildBody(smallMessageData: MessageData): RequestBody {
        val utf = MediaType.parse("text/plain; charset=utf-8")
        return RequestBody.create(utf, gson.toJson(smallMessageData.body))
    }

    protected fun buildBody(any: Any): RequestBody {
        val utf = MediaType.parse("text/plain; charset=utf-8")
        return RequestBody.create(utf, any.toString())
    }

    protected fun buildLargeBody(any: Any?): RequestBody {
        val bytes: ByteArray = when (any) {
            is File -> any.readBytes()
            is String -> any.toByteArray()
            else -> ByteArray(0)
        }
        return RequestBody.create(MediaType.parse("application/octet-stream"), bytes)
    }

    protected fun buildLargeHeaders(any: Any?): HashMap<String, String> {
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

    @SuppressLint("HardwareIds")
    protected fun buildHeaders(smallMessageData: MessageData): HashMap<String, String> {
        val map = HashMap<String, String>()
        smallMessageData.eventId?.let {
            map[StoryHeaders.EID] = it
        }
        smallMessageData.userId?.let {
            map[StoryHeaders.UID] = it
        }
        smallMessageData.correlationToken?.let {
            map[StoryHeaders.CT] = it
        }
        smallMessageData.id?.let {
            map[StoryHeaders.ID] = it
        }
        smallMessageData.operationType?.let {
            map[StoryHeaders.CUD] = it
        }
        map[StoryHeaders.DID] = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        map[StoryHeaders.M] = Build.MODEL
//        map[StoryHeaders.SN] = "need user permission"
        map[StoryHeaders.OS] = "Android"
        map[StoryHeaders.OSV] = Build.VERSION.RELEASE
        map[StoryHeaders.AN] = appName
        map[StoryHeaders.AV] = appVersion
        map[StoryHeaders.LT] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault())
            .format(smallMessageData.created)/*"2020-05-28T09:02:49.5754586"*/
        map[StoryHeaders.TZ] = (TimeZone.getDefault().rawOffset / DateUtils.HOUR_IN_MILLIS).toInt().toString()
        map[StoryHeaders.GEO] = if (smallMessageData.location != null) {
            "on;${smallMessageData.location.lat},${smallMessageData.location.lng}"
        } else {
            "off"
        }
        map[StoryHeaders.NS] = NetworkUtil.getConnectivityStatusString(context)
        map[StoryHeaders.LNG] = Locale.getDefault().language
        return map
    }
}