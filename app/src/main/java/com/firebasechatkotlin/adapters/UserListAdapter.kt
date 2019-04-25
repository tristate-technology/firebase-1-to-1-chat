package com.firebasechatkotlin.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebasechatkotlin.R
import com.firebasechatkotlin.listeners.OnItemClickListener
import com.firebasechatkotlin.models.User
import kotlinx.android.synthetic.main.row_user_list.view.*

class UserListAdapter(val context: Context, val data: ArrayList<User>) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_user_list, p0, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        var user = data[p1]

        p0.tvName.text = user.displayname
        p0.tvEmailId.text = user.email
        p0.llMain.setOnClickListener {
            if (listener != null) {
                listener?.onItemClick(p0.llMain, p1)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.tvName
        val tvEmailId = view.tvEmailId
        val llMain = view.llMain
    }

    public fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}