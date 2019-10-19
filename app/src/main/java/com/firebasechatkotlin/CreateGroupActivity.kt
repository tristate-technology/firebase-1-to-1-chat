package com.firebasechatkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebasechatkotlin.adapters.CreateGroupUserListAdapter
import com.firebasechatkotlin.models.GroupModel
import com.firebasechatkotlin.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_create_group.*
import java.io.File

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {


    val TAG = javaClass.simpleName

    var loggedUser: FirebaseUser? = null
    var userList: ArrayList<User>? = ArrayList()
    var createGroupListAdapter: CreateGroupUserListAdapter? = null
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        context = this;
        loggedUser = FirebaseAuth.getInstance().currentUser

        getData();

        ivClose.setOnClickListener(this)
        tvCreateGroup.setOnClickListener(this)
        ivGroupProfile.setOnClickListener(this)

    }

    private fun chooseImageIntent() {
        var intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(
            Intent.createChooser(intent, "Select Image"),
            SELECT_IMAGE_REQUEST
        )
    }

    var SELECT_IMAGE_REQUEST: Int = 1001
    var filePath: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.data

            try {
                var bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
                ivGroupProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getData() {
        var ref = FirebaseDatabase.getInstance().reference
        var query = ref.child("users")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                userList?.clear();
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
                    }
                }
                setAdapter()
            }
        })
    }

    private fun setAdapter() {
        if (createGroupListAdapter == null) {
            createGroupListAdapter = CreateGroupUserListAdapter(this, userList!!)
            rvUsers.layoutManager = LinearLayoutManager(this)
            rvUsers.adapter = createGroupListAdapter
        } else
            createGroupListAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivClose -> finish()

            R.id.ivGroupProfile -> {
                chooseImageIntent()
            }

            R.id.tvCreateGroup -> {
                if (filePath == null) {
                    Toast.makeText(context, "Please select profile image", Toast.LENGTH_SHORT)
                        .show()
                    return
                } else if (TextUtils.isEmpty(edtGroupName.text.toString().trim())) {
                    Toast.makeText(context, "Please enter group name", Toast.LENGTH_SHORT).show()
                    return
                }
                tvCreateGroup.visibility = View.GONE
                progress.visibility = View.VISIBLE
                Log.e(
                    TAG,
                    " selected items : " + createGroupListAdapter?.data?.filter { it.selected == true }?.size
                )
                var selectedUsers: MutableList<User>? =
                    createGroupListAdapter?.data?.filter { it.selected == true } as MutableList<User>

                if (selectedUsers?.size!! > 0) {
                    selectedUsers.add(
                        User(
                            loggedUser?.uid!!,
                            loggedUser?.displayName!!,
                            loggedUser?.email!!,
                            true
                        )
                    )
                    val key = FirebaseDatabase.getInstance().reference.child("groups").push().key
                    val groupKey =
                        FirebaseDatabase.getInstance().reference.child("groups").child(key!!).push()
                            .key


                    var fireStorageReference: StorageReference =
                        FirebaseStorage.getInstance().reference
                    var file = File(filePath.toString())
                    var storageRef = fireStorageReference.child("images/" + groupKey + file.name)
                    storageRef.putFile(filePath!!)
                        .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    var firebase =
                                        FirebaseDatabase.getInstance().reference.child("groups")
                                            .child(groupKey!!)
                                    firebase.addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            Toast.makeText(
                                                this@CreateGroupActivity,
                                                "Group has been created",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            setResult(Activity.RESULT_OK)
                                            finish()
                                        }

                                    })

                                    firebase.setValue(
                                        GroupModel(
                                            edtGroupName.text.toString().trim(),
                                            selectedUsers,
                                            groupKey,
                                            uri.toString()
                                        )
                                    )

                                }
                            }
                        }).addOnFailureListener(object : OnFailureListener {
                            override fun onFailure(p0: java.lang.Exception) {
                                progress.visibility = View.GONE
                                tvCreateGroup.visibility = View.VISIBLE
                            }

                        })
                }
            }
        }
    }

}