package ru.breffi.lib.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Body : RealmObject() {
    @PrimaryKey
    var id : String? = null
}