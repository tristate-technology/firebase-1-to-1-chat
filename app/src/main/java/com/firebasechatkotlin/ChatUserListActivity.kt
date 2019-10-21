package com.firebasechatkotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebasechatkotlin.adapters.GroupListAdapter
import com.firebasechatkotlin.adapters.UserListAdapter
import com.firebasechatkotlin.listeners.OnItemClickListener
import com.firebasechatkotlin.models.GroupModel
import com.firebasechatkotlin.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat_user_list.*


class ChatUserListActivity : AppCompatActivity(), OnItemClickListener {


    private var loggedUser: FirebaseUser? = null
    private var userList: ArrayList<User>? = ArrayList()
    private var groupList: ArrayList<GroupModel>? = ArrayList()
    private var adapter: UserListAdapter? = null
    private var groupsAdapter: GroupListAdapter? = null
    private var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_user_list)

        setTitle(R.string.chat_user_title)

        loggedUser = FirebaseAuth.getInstance().currentUser

        getData()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 0, 0, "Create Group")
        menu?.add(0, 1, 0, "Profile")
        menu?.add(0, 2, 0, "Logout")
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> startActivityForResult(Intent(this, CreateGroupActivity::class.java), 11)

            2 -> logOut()

            1 -> user.let {
                startActivity(
                    Intent(this, ProfileActivity::class.java).putExtra(
                        "user",
                        user
                    )
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun logOut() {

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getData() {

        val ref = FirebaseDatabase.getInstance().reference
        val usersQuery = ref.child("users")
        val groupsQuery = ref.child("groups")

        groupsQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                progress.visibility = View.GONE
                groupList?.clear()

                for (data in p0.children) {
                    var users: ArrayList<User> = ArrayList()
                    for (user in data.child("users").children) {
                        users.add(
                            User(
                                user.child("uid").value as String,
                                user.child("displayname").value as String,
                                user.child("email").value as String,
                                false,
                                user.child("profile").value as String
                            )
                        )
                    }
                    if (users.any { it -> it.uid == loggedUser?.uid }) {
                        var groupModel: GroupModel = GroupModel(
                            data.child("groupname").value as String,
                            data.child("users").value as List<User>,
                            data.child("groupKey").value as String,
                            data.child("profile").value as String
                        )
                        groupList?.add(groupModel)
                    }
                }
                setAdapterForGroups()
            }

        })

        usersQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progress.visibility = View.GONE
            }

            override fun onDataChange(p0: DataSnapshot) {
                progress.visibility = View.GONE
                userList?.clear()
                for (data in p0.children) {
                    if (data.child("uid").value as String != loggedUser?.uid) {
                        var user: User = User(
                            data.child("uid").value as String,
                            data.child("displayname").value as String,
                            data.child("email").value as String,
                            false,
                            data.child("profile").value as String
                        )
                        userList?.add(user)
                    } else
                        user = User(
                            data.child("uid").value as String,
                            data.child("displayname").value as String,
                            data.child("email").value as String,
                            false,
                            data.child("profile").value as String
                        )
                }
                setAdapter()
            }

        })
    }

    private fun setAdapterForGroups() {
        if (groupsAdapter == null) {
            groupsAdapter = GroupListAdapter(this, groupList!!)
            groupsAdapter?.setOnItemClickListener(this)
            rvGroupsList.layoutManager = LinearLayoutManager(this)
            rvGroupsList.adapter = groupsAdapter
        } else
            adapter?.notifyDataSetChanged()
    }

    private fun setAdapter() {
        if (adapter == null) {
            adapter = UserListAdapter(this, userList!!)
            adapter?.setOnItemClickListener(this)
            rvUserList.layoutManager = LinearLayoutManager(this)
            rvUserList.adapter = adapter
        } else {
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onItemClick(view: View, position: Int, key: String) {
        when (view.id) {
            R.id.llMain -> {
                if (key == "users")
                    ChatActivity.startActivity(this, userList?.get(position)!!, loggedUser?.uid!!)
                else if (key == "group") {
                    var intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("group", groupList?.get(position))
                    intent.putExtra("loggedUserId", loggedUser?.uid)
                    intent.putExtra("user", user)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getData()
    }
}
