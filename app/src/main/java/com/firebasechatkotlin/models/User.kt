package com.firebasechatkotlin.models

import java.io.Serializable

data class User(
    val uid: String,
    var displayname: String,
    val email: String,
    var selected: Boolean = false,
    val profile: String=""
) : Serializable {

}