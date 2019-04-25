package com.firebasechatkotlin.models

import java.sql.Timestamp

data class Message(val message: String, val senderId: String, val receiverId: String, val timestamp: Long){

}