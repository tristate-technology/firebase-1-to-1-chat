package com.firebasechatkotlin.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.facebook.drawee.view.SimpleDraweeView
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
                listener?.onItemClick(p0.llMain, p1, "users")
            }
        }
        p0.ivProfile.setImageURI(user.profile)

        p0.ivProfile.setOnClickListener {
            setImagePopup(user.profile)
        }


//        if (user.selected)
//            p0.llMain.setSelected(true)
//        else
//            p0.llMain.setSelected(false)

    }

    fun setImagePopup(p0: String) {
        var imagePopup: ImagePopup = ImagePopup(context)
//            imagePopup.windowHeight = 800
//            imagePopup.windowWidth = 800
        imagePopup.setFullScreen(true)
        imagePopup.backgroundColor = Color.BLACK
        imagePopup.setImageOnClickClose(true)
        imagePopup.isHideCloseIcon = false
        imagePopup.initiatePopupWithGlide(p0)

//        imagePopup.initiatePopup(p0.drawable)

        imagePopup.viewPopup()
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.tvName
        val tvEmailId = view.tvEmailId
        val llMain: LinearLayout = view.llMain
        val ivProfile: SimpleDraweeView = view.ivProfile
    }

    public fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


}