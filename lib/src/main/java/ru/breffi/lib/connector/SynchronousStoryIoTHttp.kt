package ru.breffi.lib.connector

import android.content.Context
import com.google.gson.Gson
import ru.breffi.lib.models.IoTConfig
import ru.breffi.lib.models.IoTResult
import ru.breffi.lib.models.MessageData
import ru.breffi.lib.network.FeedResponse
import ru.breffi.lib.network.MessageResponse
import ru.breffi.lib.network.StoryIoTService

class SynchronousStoryIoTHttp constructor(
    context: Context,
    gson: Gson,
    private val storyIoTService: StoryIoTService,
    appName: String,
    appVersion: String,
    private val config: IoTConfig
) : BaseStoryIoTHttpConnector(context, gson, appName, appVersion, config) {

    companion object {
        const val TAG = "SynchronousIoTHttp"
    }

    fun publishSmallMessage(messageData: MessageData): IoTResult<MessageResponse> {
        val expiration = getExpiration()
        val response = storyIoTService
            .publishSmallMessageCall(
                config.hub,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
                buildBody(messageData),
                buildHeaders(messageData)
            )
            .execute()
        return IoTResult(response.body(), response.code(), response.errorBody()?.string())
    }

    fun publishLargeMessage(messageData: MessageData): IoTResult<MessageResponse> {
        val expiration = getExpiration()
        val firstResponse = storyIoTService
            .publishLargeMessageFirstCall(
                config.hub,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
                buildHeaders(messageData)
            )
            .execute()
        val firstBody = firstResponse.body()
        if (firstBody != null) {
            val secondResponse = storyIoTService
                .publishLargeMessageSecondCall(
                    firstBody.path,
                    buildLargeBody(messageData.body),
                    buildLargeHeaders(messageData.body)
                )
                .execute()
            val secondBody = secondResponse.body()
            if (secondBody != null) {
                val publishResponse = storyIoTService
                    .confirmLargeMessagePublicationCall(
                        config.hub,
                        firstBody.id,
                        config.key,
                        expiration,
                        buildSignature(config.key, expiration)
                    )
                    .execute()
                return IoTResult(publishResponse.body(), publishResponse.code(), publishResponse.errorBody()?.string())
            } else {
                return IoTResult(null, secondResponse.code(), secondResponse.errorBody()?.string())
            }
        } else {
            return IoTResult(null, firstResponse.code(), firstResponse.errorBody()?.string())
        }
    }

    fun getFeed(token: String?, direction: String, size: Int): IoTResult<FeedResponse> {
        val expiration = getExpiration()
        val feedResponse = storyIoTService
            .getFeedCall(
                config.hub,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
                token,
                direction,
                size
            )
            .execute()
        val messageListResponse = feedResponse.body()
        val errorString = feedResponse.errorBody()?.string()
        if (messageListResponse != null) {
            return IoTResult(
                FeedResponse(messageListResponse.body(), messageListResponse.headers().get("Cursor-Position")),
                feedResponse.code(),
                errorString
            )
        } else {
            return IoTResult(null, feedResponse.code(), errorString)
        }
    }

    fun getMessage(id: String?): IoTResult<MessageResponse> {
        val expiration = getExpiration()
        val response = storyIoTService
            .getStorageMessageCall(
                config.hub,
                id,
                config.key,
                expiration,
                buildSignature(config.key, expiration)
            )
            .execute()
        return IoTResult(response.body(), response.code(), response.errorBody()?.string())
    }

    fun updateMetadataMessage(metaName: String, metaValue: String, id: String): IoTResult<MessageResponse> {
        val expiration = getExpiration()
        val response = storyIoTService
            .updateMetadataMessageCall(
                config.hub,
                id,
                metaName,
                config.key,
                expiration,
                buildSignature(config.key, expiration),
                buildBody(metaValue)
            ).execute()
        return IoTResult(response.body(), response.code(), response.errorBody()?.string())
    }

    fun deleteMetadataMessage(metaName: String, id: String): IoTResult<MessageResponse> {
        val expiration = getExpiration()
        val response = storyIoTService
            .deleteMetadataMessageCall(
                config.hub,
                id,
                metaName,
                config.key,
                expiration,
                buildSignature(config.key, expiration)
            )
            .execute()
        return IoTResult(response.body(), response.code(), response.errorBody()?.string())
    }
}