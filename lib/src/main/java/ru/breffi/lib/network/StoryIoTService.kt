package ru.breffi.lib.network

import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    ): Observable<SmallMessageResponse>

    @POST("/{hub}/publish")
    fun publishLargeMessageFirst(
        @Path("hub") hub: String,
        @Query("key", encoded = true) key: String,
        @Query("expiration", encoded = true) expiration: String,
        @Query("signature", encoded = true) signature: String,
        @HeaderMap headers: Map<String, String>
    ): Observable<SmallMessageResponse>

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
    ): Observable<ResponseBody>

}
