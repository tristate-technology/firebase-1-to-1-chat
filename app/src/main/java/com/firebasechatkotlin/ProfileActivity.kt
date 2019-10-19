package com.firebasechatkotlin

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.firebasechatkotlin.models.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    var TAG: String = javaClass.simpleName

    var user: User? = null
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setTheme(R.style.AppTheme)

        context = this@ProfileActivity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        user = intent.getSerializableExtra("user") as User

        supportActionBar?.title = "Profile"

        tvDisplayName.text = user?.displayname
        tvDisplayEmail.text = user?.email
        ivProfile.setImageURI(user?.profile)

        ivProfile.setOnClickListener { setImagePopup(user?.profile!!) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setImagePopup(p0: String) {
        var imagePopup: ImagePopup = ImagePopup(this)
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
}