package ru.breffi.lib

import android.util.Base64
import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Signature {
    companion object {
        const val TAG = "Signature"
        private const val PRIVATE_KEY = "163af6783ae14d5f829288d1ca44950e"

        fun create(key: String, expiration: String): String {
            val message = "key=${key}expiration=$expiration"
            Log.e(TAG, "message = $message")
            val shaHMAC = Mac.getInstance("HmacSHA512")
            val secretKey = SecretKeySpec(PRIVATE_KEY.toByteArray(), "HmacSHA512")
            shaHMAC.init(secretKey)
            var hash = Base64.encodeToString(shaHMAC.doFinal(message.toByteArray()), Base64.DEFAULT)
            Log.e(TAG, "base64 = $hash")
            hash = hash.replace("/", "_")
                .replace("+", "-")
            Log.e(TAG, "base64 with replace = $hash")
            return hash
        }
    }
}