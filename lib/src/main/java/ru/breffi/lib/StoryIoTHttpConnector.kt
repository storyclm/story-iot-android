package ru.breffi.lib

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import ru.breffi.lib.models.StoryMessage
import ru.breffi.lib.network.*
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class StoryIoTHttpConnector private constructor(
    private val context: Context,
    private val gson: Gson,
    private val storyIoTService: StoryIoTService,
    private val appName: String,
    private val appVersion: String
) {
    data class Builder(val context: Context) {

        private var appName: String? = null
        private var appVersion: String? = null
        private var smallMessages: ArrayList<StoryMessage>? = null
        private var gson: Gson? = null

        fun setAppName(appName: String) = apply { this.appName = appName }
        fun setAppVersion(appVersion: String) = apply { this.appVersion = appVersion }
        fun setSmallMessages(smallMessages: ArrayList<StoryMessage>?) = apply { this.smallMessages = smallMessages }
        fun setGson(gson: Gson) = apply { this.gson = gson }

        fun build(): StoryIoTHttpConnector {
            val gson = gson?: GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
            val storyIoTService = Communicator.getStoryIoTService(gson)
            return StoryIoTHttpConnector(
                context,
                gson,
                storyIoTService,
                appName ?: "",
                appVersion ?: ""
            )
        }
    }

    companion object {
        const val TAG = "StoryIoTHttpConnector"
        val endpoint = "https://staging-iot.storychannels.app"
        val hub = "b47bbc659eb344888f9f92ed3261d8dc"
        val key = "df94b12c3355425eb4efa406f09e8b9f"
        val expiration = "2020-05-28T09:02:49.5754586Z"
    }

    fun publishSmallMessage(storyMessage: StoryMessage): Observable<MessageResponse> {
        return storyIoTService.publishSmallMessage(
            hub,
            key,
            expiration,
            Signature.create(key, expiration),
            buildBody(storyMessage),
            buildHeaders(storyMessage)
        )
    }

    fun publishLargeMessage(storyMessage: StoryMessage): Observable<MessageResponse> {
        return storyIoTService.publishLargeMessageFirst(
            hub,
            key,
            expiration,
            Signature.create(key, expiration),
            buildHeaders(storyMessage)
        )
            .flatMap { messageResponse ->
                storyIoTService.publishLargeMessageSecond(
                    messageResponse.path,
                    buildLargeBody(storyMessage.body),
                    buildLargeHeaders(storyMessage.body)
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
    }

    fun getFeed(token: String?, direction: String, size: Int): Observable<FeedResponse> {
        return storyIoTService.getFeed(
            hub,
            key,
            expiration,
            Signature.create(key, expiration),
            token,
            direction,
            size
        )
            .map { messageResponse ->
                FeedResponse(
                    messageResponse.body(),
                    messageResponse.headers().get("Cursor-Position")
                )
            }
    }

    fun getMessage(id: String?): Observable<MessageResponse> {
        return storyIoTService.getStorageMessage(
            hub,
            id,
            key,
            expiration,
            Signature.create(key, expiration)
        )
    }

    fun updateMetadataMessage(metaName: String, metaValue: String, id: String): Observable<MessageResponse> {
        return storyIoTService.updateMetadataMessage(
            hub,
            id,
            metaName,
            key,
            expiration,
            Signature.create(key, expiration),
            buildBody(metaValue)
        )
    }

    fun deleteMetadataMessage(metaName: String, id: String): Observable<MessageResponse> {
        return storyIoTService.deleteMetadataMessage(
            hub,
            id,
            metaName,
            key,
            expiration,
            Signature.create(key, expiration)
        )
    }

    private fun buildBody(smallMessage: StoryMessage): RequestBody {
        val utf = MediaType.parse("text/plain; charset=utf-8")
        return RequestBody.create(utf, gson.toJson(smallMessage.body))
    }

    private fun buildBody(any: Any): RequestBody {
        val utf = MediaType.parse("text/plain; charset=utf-8")
        return RequestBody.create(utf, any.toString())
    }

    private fun buildLargeBody(any: Any?): RequestBody {
        val bytes: ByteArray = when (any) {
            is File -> any.readBytes()
            is String -> any.toByteArray()
            else -> ByteArray(0)
        }
        return RequestBody.create(MediaType.parse("application/octet-stream"), bytes)
    }

    private fun buildLargeHeaders(any: Any?): HashMap<String, String> {
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

    private fun buildHeaders(smallMessage: StoryMessage): HashMap<String, String> {
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
//        map[StoryHeaders.SN] = "need user permission"
        map[StoryHeaders.OS] = "Android"
        map[StoryHeaders.OSV] = Build.VERSION.RELEASE
        map[StoryHeaders.AN] = appName
        map[StoryHeaders.AV] = appVersion
        map[StoryHeaders.IT] = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
            Locale.getDefault()
        ).format(Date())/*"2020-05-28T09:02:49.5754586"*/
        map[StoryHeaders.TZ] = (TimeZone.getDefault().rawOffset / DateUtils.HOUR_IN_MILLIS).toInt().toString()
        map[StoryHeaders.GEO] = "off"/*"on;-34.8799074,174.7565664"*/
        map[StoryHeaders.NS] = NetworkUtil.getConnectivityStatusString(context)
        map[StoryHeaders.LNG] = Locale.getDefault().language
        return map
    }
}