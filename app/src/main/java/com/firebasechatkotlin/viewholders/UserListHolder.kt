package com.firebasechatkotlin.viewholders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.firebasechatkotlin.R
import com.firebasechatkotlin.models.User
import kotlinx.android.synthetic.main.row_user_list.view.*

class UserListHolder(customView: View) : RecyclerView.ViewHolder(customView) {

    fun bindMessage(user: User?) {
        with(user!!) {
            itemView.tvName.text = user.displayname
            itemView.tvEmailId.text = user.email
        }
    }

}