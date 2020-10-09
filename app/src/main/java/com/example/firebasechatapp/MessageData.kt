package com.example.firebasechatapp

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MessageData (
    var text : String? ="",
    var name : String ="",
    var photoUrl : String? = ""
) {
    @Exclude
    fun toMap() : Map<String, Any?> {
        return mapOf(
            "name" to name,
            "photoUrl" to photoUrl,
            "text" to text
        )
    }
}