package com.firebasechatkotlin.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.firebasechatkotlin.R
import com.firebasechatkotlin.listeners.OnItemClickListener
import com.firebasechatkotlin.models.Message
import kotlinx.android.synthetic.main.row_my_message.view.*

class ChatAdapter(
    val context: Context,
    val data: ArrayList<Message>,
    val loggedUId: String,
    val isGroupChat: Boolean
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, type: Int): ViewHolder {
//        return if (type == 1) {
//            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_others_message, p0, false))
//        } else {
//            ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_my_message, p0, false))
//        }
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_my_message, p0, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        var message = data[p1]

        if (getItemViewType(p1) == 1) {
            p0.relLeft.visibility = View.VISIBLE
            p0.relRight.visibility = View.GONE
            p0.tvLeftMessage.text = message.message
            p0.tvLeftName.text = message.user.displayname
            if (isGroupChat) {
                p0.ivLeftProfile.visibility = View.VISIBLE
                p0.tvLeftName.visibility = View.VISIBLE
                p0.tvLeftTimeDate.text = getRelativeTimeString(message.timestamp)
                p0.ivLeftProfile.setImageURI(message.user.profile)
            } else {
                p0.ivLeftProfile.visibility = View.GONE
                p0.tvLeftName.visibility = View.GONE
            }
        } else {
            p0.relLeft.visibility = View.GONE
            p0.relRight.visibility = View.VISIBLE
            p0.tvRightMessage.text = message.message
            p0.tvRightTimeDate.text = getRelativeTimeString(message.timestamp)
            if (isGroupChat) {
                p0.ivRightProfile.visibility = View.VISIBLE
                p0.tvRightName.visibility = View.VISIBLE
                p0.ivRightProfile.setImageURI(message.user.profile)
                p0.tvRightName.text = message.user.displayname
            } else {
                p0.ivRightProfile.visibility = View.GONE
                p0.tvRightName.visibility = View.GONE
            }
        }

//        if (isGroupChat) {
//            p0.tvName.visibility = VISIBLE
//            p0.ivProfileImage.visibility = VISIBLE
//        } else {
//            p0.tvName.visibility = GONE
//           p0.ivProfileImage.visibility = GONE
//        }
        p0.ivRightProfile.setOnClickListener { setImagePopup(message.user.profile) }
        p0.ivLeftProfile.setOnClickListener { setImagePopup(message.user.profile) }
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

    override fun getItemViewType(position: Int): Int {
        return if (loggedUId.equals(data.get(position).senderId)) {
            0
        } else {
            1
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val relRight = view.rel_right
        val tvRightName = view.rightTvName
        val tvRightMessage = view.rightTvMessage
        val tvRightTimeDate = view.rightTvTimeDate
        val ivRightProfile = view.rightIvProfileImage
        val relLeft = view.rel_left
        val tvLeftName = view.leftTvName
        val tvLeftMessage = view.leftTvMessage
        val tvLeftTimeDate = view.leftTvTimeDate
        val ivLeftProfile = view.leftIvProfileImage
//        val ivProfileImage = view.ivProfileImage
//        val tvName = view.tvName
    }

    public fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun getRelativeTimeString(timestamp: Long): String {
        return if (System.currentTimeMillis() <= timestamp + 60000) {
            "Just Now"
        } else {
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS
            ).toString()
        }
    }
}