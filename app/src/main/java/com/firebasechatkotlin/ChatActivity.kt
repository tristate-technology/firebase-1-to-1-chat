package com.firebasechatkotlin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.firebasechatkotlin.adapters.ChatAdapter
import com.firebasechatkotlin.models.GroupModel
import com.firebasechatkotlin.models.Message
import com.firebasechatkotlin.models.User
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity(), View.OnClickListener {


    companion object {
        fun startActivity(context: Context, user: User, loggedUserId: String) {
            val intent: Intent? = Intent(context, ChatActivity::class.java)
            intent?.putExtra("user", user)
            intent?.putExtra("loggedUserId", loggedUserId)
            context.startActivity(intent)
        }
    }

    private var user: User? = null
    private var loggedUserId: String? = null;
    private var messageList: ArrayList<Message>? = ArrayList()
    private var database: FirebaseDatabase? = null
    private var adapter: ChatAdapter? = null

    private var isGroupChat: Boolean = false;
    private var group: GroupModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeCreateGroup)
        setContentView(R.layout.activity_chat)

        btnSend.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivProfile.setOnClickListener(this)

        database = FirebaseDatabase.getInstance()

        if (intent.hasExtra("group")) {
            isGroupChat = true
            group = intent.getSerializableExtra("group") as GroupModel
            tvTitle.text = group?.groupname
            user = intent.getSerializableExtra("user") as User?
            ivProfile.setImageURI(group?.profile)
        } else {
            user = intent.getSerializableExtra("user") as User?
            tvTitle.text = user?.displayname
            ivProfile.setImageURI(user?.profile)
        }

        loggedUserId = intent.getStringExtra("loggedUserId")

//        if (isGroupChat) {
//            database!!.reference.child("users").child(loggedUserId!!)
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//
//                        user = p0.getValue(User::class.java)
//
//                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//                        supportActionBar?.setHomeButtonEnabled(true)
//
//
//                    }
//
//                })
//        }

        getData()
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

    private fun setAdapter() {
        if (adapter == null) {
            rvMessage.layoutManager = LinearLayoutManager(this)
            adapter = ChatAdapter(this, messageList!!, loggedUserId!!, isGroupChat)
            rvMessage.adapter = adapter
        } else {
            adapter?.notifyDataSetChanged()
        }

        rvMessage.scrollToPosition(messageList?.size!! - 1)
    }

    private fun getData() {
        var query: DatabaseReference?
        if (isGroupChat) {
            query = database?.reference?.child("message")?.child(group?.groupKey!!)
        } else
            query =
                database?.reference?.child("message")
                    ?.child(getMessageId(loggedUserId!!, user?.uid!!))

        query?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progress.visibility = View.GONE
            }

            override fun onDataChange(p0: DataSnapshot) {
                progress.visibility = View.GONE
                messageList?.clear()
                for (data in p0.children) {
                    var message: Message? = Message(
                        data.child("message").value as String,
                        data.child("senderId").value as String,
                        data.child("receiverId").value as String,
                        data.child("timestamp").value as Long,
                        User(
                            data.child("user").child("uid").value as String,
                            data.child("user").child("displayname").value as String,
                            data.child("user").child("email").value as String,
                            false,
                            data.child("user").child("profile").value as String
                        )
                    )
                    messageList?.add(message!!)

                }

                setAdapter()
            }

        })
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        if (menuItem?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)

    }

    private fun getMessageId(uid1: String, uid2: String): String {
        if (uid1 > uid2) {
            return uid1 + uid2
        } else {
            return uid2 + uid1
        }
    }

    private fun sendMessage() {
        val key = database?.reference?.child("message")?.push()?.key
        var firebase: DatabaseReference?
        if (isGroupChat) {
            firebase = database?.reference?.child("message")?.child(group?.groupKey!!)?.child(key!!)
        } else {
            firebase =
                database?.reference?.child("message")
                    ?.child(getMessageId(loggedUserId!!, user?.uid!!))
                    ?.child(key!!)
        }


        firebase?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        var message: Message?
        if (isGroupChat) {
            message = Message(
                etMessage.text.toString().trim(),
                loggedUserId!!,
                "", System.currentTimeMillis(), user!!
            )
        } else
            message = Message(
                etMessage.text.toString().trim(),
                loggedUserId!!,
                user?.uid!!,
                System.currentTimeMillis(), user!!
            )
        firebase?.setValue(message)

        etMessage.setText("")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                if (!TextUtils.isEmpty(etMessage.text.toString().trim())) {
                    sendMessage()
                } else {
                    Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.ivBack -> finish()
            R.id.ivProfile -> if (isGroupChat) setImagePopup(group?.profile!!) else setImagePopup(
                user?.profile!!
            )
        }
    }
}



