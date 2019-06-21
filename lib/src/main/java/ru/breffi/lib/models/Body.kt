package ru.breffi.lib.models

import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Body : RealmObject() {
    @Expose
    @PrimaryKey
    var id : String? = null

    @Expose
    var value : String? = null
}