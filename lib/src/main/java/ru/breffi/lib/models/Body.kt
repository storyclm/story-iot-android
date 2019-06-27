package ru.breffi.lib.models

import com.google.gson.annotations.Expose

open class Body {
    @Expose
    var id: String? = null

    @Expose
    var value: String? = null
}