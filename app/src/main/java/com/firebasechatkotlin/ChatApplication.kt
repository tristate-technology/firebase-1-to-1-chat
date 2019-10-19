package com.firebasechatkotlin

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class ChatApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}