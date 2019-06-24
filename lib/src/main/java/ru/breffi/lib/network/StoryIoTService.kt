package ru.breffi.lib.network

import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface StoryIoTService {
    @POST("/{hub}/publish")
    fun publishSmallMessage(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Observable<MessageResponse>

    @POST("/{hub}/publish")
    fun publishLargeMessageFirst(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @HeaderMap headers: Map<String, String>
    ): Observable<MessageResponse>

    @PUT
    fun publishLargeMessageSecond(
        @Url url: String,
        @Body body: RequestBody?,
        @HeaderMap headers: Map<String, String>
    ): Observable<ResponseBody>


    @PUT("/{hub}/publish/{id}/confirm")
    fun confirmLargeMessagePublication(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Observable<MessageResponse>

    @GET("/{hub}/feed")
    fun getFeed(
        @Path("hub") hub: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Query("token", encoded = true) token: String?,
        @Query("direction", encoded = true) direction: String?,
        @Query("size", encoded = true) size: Int?
    ): Observable<Response<List<MessageResponse>>>

    @GET("/{hub}/storage/{id}")
    fun getStorageMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Observable<MessageResponse>

    @PUT("/{hub}/storage/{id}/meta/{meta}")
    fun updateMetadataMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?,
        @Body body: RequestBody?
    ): Observable<MessageResponse>

    @DELETE("/{hub}/storage/{id}/meta/{meta}")
    fun deleteMetadataMessage(
        @Path("hub") hub: String?,
        @Path("id") id: String?,
        @Path("meta") meta: String?,
        @Query("key", encoded = true) key: String?,
        @Query("expiration", encoded = true) expiration: String?,
        @Query("signature", encoded = true) signature: String?
    ): Observable<MessageResponse>
}
