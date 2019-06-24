package ru.breffi.lib.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Metadata (
	@SerializedName("eid") @Expose val eid : String,
	@SerializedName("did") @Expose val did : String,
	@SerializedName("uid") @Expose val uid : String,
	@SerializedName("ct") @Expose val ct : String,
	@SerializedName("lng") @Expose val lng : String,
	@SerializedName("id") @Expose val id : String,
	@SerializedName("cud") @Expose val cud : String,
	@SerializedName("m") @Expose val m : String,
	@SerializedName("sn") @Expose val sn : String,
	@SerializedName("os") @Expose val os : String,
	@SerializedName("osv") @Expose val osv : String,
	@SerializedName("an") @Expose val an : String,
	@SerializedName("av") @Expose val av : String,
	@SerializedName("it") @Expose val it : String,
	@SerializedName("tz") @Expose val tz : String,
	@SerializedName("geo") @Expose val geo : String,
	@SerializedName("hit") @Expose val hit : String,
	@SerializedName("ns") @Expose val ns : String
)