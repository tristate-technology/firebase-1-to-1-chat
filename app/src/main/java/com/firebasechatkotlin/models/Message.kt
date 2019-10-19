package com.firebasechatkotlin.models

import java.io.Serializable

data class Message(
    val message: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long,
    val user: User
) : Serializable {

}