package ru.breffi.lib.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Metadata (
	@SerializedName("eid") @Expose val eid : String,
	@SerializedName("did") @Expose val did : String,
	@SerializedName("uid") @Expose val uid : String,
	@SerializedName("ct") @Expose val ct : String
)