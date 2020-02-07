package ru.breffi.lib.connector

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Single
import ru.breffi.lib.models.IoTConfig
import ru.breffi.lib.models.MessageData
import ru.breffi.lib.network.FeedResponse
import ru.breffi.lib.network.MessageResponse
import ru.breffi.lib.network.StoryIoTService

class RxStoryIoTHttp internal constructor(
    context: Context,
    gson: Gson,
    private val storyIoTService: StoryIoTService,
    appName: String,
    appVersion: String,
    private val config: IoTConfig
) : BaseStoryIoTHttpConnector(context, gson, appName, appVersion, config) {

    companion object {
        const val TAG = "RxIoTHttp"
    }

    fun publishSmallMessage(messageData: MessageData): Single<MessageResponse> {
        val expiration = getExpiration()
        return storyIoTService.publishSmallMessage(
            config.hub,
            config.key,
            expiration,
            buildSignature(config.key, expiration),
            buildBody(messageData),
            buildHeaders(messageData)
        )
    }

    fun publishLargeMessage(messageData: MessageData): Single<MessageResponse> {
        val expiration = getExpiration()
        return storyIoTService
            .publishLargeMessageFirst(
                config.hub,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
                buildHeaders(messageData)
            )
            .flatMap { messageResponse ->
                storyIoTService
                    .publishLargeMessageSecond(
                        messageResponse.path,
                        buildLargeBody(messageData.body),
                        buildLargeHeaders(messageData.body)
                    )
                    .flatMap { responseBody ->
                        storyIoTService.confirmLargeMessagePublication(
                            config.hub,
                            messageResponse.id,
                            config.key,
                            expiration,
                            buildSignature(config.key, expiration)
                        )
                    }
            }
    }

    fun getFeed(token: String?, direction: String, size: Int): Single<FeedResponse> {
        val expiration = getExpiration()
        return storyIoTService
            .getFeed(
                config.hub,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
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

    fun getMessage(id: String?): Single<MessageResponse> {
        val expiration = getExpiration()
        return storyIoTService.getStorageMessage(
            config.hub,
            id,
            config.key,
            expiration,
            buildSignature(config.key, expiration)
        )
    }

    fun updateMetadataMessage(metaName: String, metaValue: String, id: String): Single<MessageResponse> {
        val expiration = getExpiration()
        return storyIoTService.updateMetadataMessage(
            config.hub,
            id,
            metaName,
            config.key,
            expiration,
            buildSignature(config.key, expiration),
            buildBody(metaValue)
        )
    }

    fun deleteMetadataMessage(metaName: String, id: String): Single<MessageResponse> {
        val expiration = getExpiration()
        return storyIoTService.deleteMetadataMessage(
            config.hub,
            id,
            metaName,
            config.key,
            expiration,
            buildSignature(config.key, expiration)
        )
    }
}