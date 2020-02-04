package ru.breffi.lib

import android.util.Base64
import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SignatureBuilder(private val privateKey: String) {

    private val parameters = mutableListOf<Pair<String, String>>()

    companion object {
        const val TAG = "Signature"
    }

    fun addParam(key: String, value: String) = apply {
        parameters.add(Pair(key, value))
    }

    fun build(): String {
        val message = parameters
            .joinTo(StringBuilder(), "") { "${it.first}=${it.second}" }
            .toString()
        Log.e(TAG, "message = $message")
        val shaHMAC = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(privateKey.toByteArray(), "HmacSHA512")
        shaHMAC.init(secretKey)
        var hash = Base64.encodeToString(shaHMAC.doFinal(message.toByteArray()), Base64.NO_WRAP)
        Log.e(TAG, "base64 = $hash")
        hash = hash.replace("/", "_")
            .replace("+", "-")
        Log.e(TAG, "base64 with replace = $hash")
        return hash
    }
}