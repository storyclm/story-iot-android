package ru.breffi.lib.network

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface StoryIoTService {

    //reactive

    @POST("/{hub}/publish")
    fun publishSmallMessage(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Single<MessageResponse>

    @POST("/{hub}/publish")
    fun publishLargeMessageFirst(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @HeaderMap headers: Map<String, String>
    ): Single<MessageResponse>

    @PUT
    fun publishLargeMessageSecond(
        @Url url: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Single<ResponseBody>


    @PUT("/{hub}/publish/{id}/confirm")
    fun confirmLargeMessagePublication(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Single<MessageResponse>

    @GET("/{hub}/feed")
    fun getFeed(
        @Path("hub") hub: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Query("token", encoded = true) token: String?,
        @Query("direction", encoded = true) direction: String?,
        @Query("size", encoded = true) size: Int?
    ): Single<Response<List<MessageResponse>>>

    @GET("/{hub}/storage/{id}")
    fun getStorageMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Single<MessageResponse>

    @PUT("/{hub}/storage/{id}/meta/{meta}")
    fun updateMetadataMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Body body: RequestBody?
    ): Single<MessageResponse>

    @DELETE("/{hub}/storage/{id}/meta/{meta}")
    fun deleteMetadataMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Single<MessageResponse>


    //blocking
    @POST("/{hub}/publish")
    fun publishSmallMessageCall(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Call<MessageResponse>

    @POST("/{hub}/publish")
    fun publishLargeMessageFirstCall(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @HeaderMap headers: Map<String, String>
    ): Call<MessageResponse>

    @PUT
    fun publishLargeMessageSecondCall(
        @Url url: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>


    @PUT("/{hub}/publish/{id}/confirm")
    fun confirmLargeMessagePublicationCall(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Call<MessageResponse>

    @GET("/{hub}/feed")
    fun getFeedCall(
        @Path("hub") hub: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Query("token", encoded = true) token: String?,
        @Query("direction", encoded = true) direction: String?,
        @Query("size", encoded = true) size: Int?
    ): Call<Response<List<MessageResponse>>>

    @GET("/{hub}/storage/{id}")
    fun getStorageMessageCall(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Call<MessageResponse>

    @PUT("/{hub}/storage/{id}/meta/{meta}")
    fun updateMetadataMessageCall(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Body body: RequestBody?
    ): Call<MessageResponse>

    @DELETE("/{hub}/storage/{id}/meta/{meta}")
    fun deleteMetadataMessageCall(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Call<MessageResponse>
}
