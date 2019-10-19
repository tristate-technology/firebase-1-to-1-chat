package com.firebasechatkotlin.listeners

import android.view.View

interface OnItemClickListener {
    fun onItemClick(view: View, position: Int,key:String)
}