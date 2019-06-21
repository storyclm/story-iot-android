package ru.breffi.lib.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SmallMessageResponse(
    @SerializedName("Path") @Expose val path: String,
    @SerializedName("Topic") @Expose val topic: String,
    @SerializedName("Metadata") @Expose val metadata: Metadata,
    @SerializedName("Hash") @Expose val hash: String,
    @SerializedName("Length") @Expose val length: Int,
    @SerializedName("Id") @Expose val id: String,
    @SerializedName("Ticks") @Expose val ticks: Long
)