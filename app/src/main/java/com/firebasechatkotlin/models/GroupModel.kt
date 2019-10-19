package com.firebasechatkotlin.models

import java.io.Serializable

data class GroupModel(
    var groupname: String,
    var users: List<User>,
    var groupKey: String = "",
    var profile: String=""
) : Serializable {

}