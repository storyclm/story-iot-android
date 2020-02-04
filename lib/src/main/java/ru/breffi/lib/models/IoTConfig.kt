package ru.breffi.lib.models

data class IoTConfig(
    val endpoint: String,
    val hub: String,
    val key: String,
    val privateKey: String,
    val expirationInSeconds: Int = 180
)