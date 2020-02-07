package ru.breffi.lib.models

data class IoTResult<T>(val data: T? = null, val code: Int, val errorMessage: String? = null)