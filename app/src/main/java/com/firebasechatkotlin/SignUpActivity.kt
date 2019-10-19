package com.firebasechatkotlin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.firebasechatkotlin.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.File


class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private val REQUEST_PERMISSION: Int = 1002
    private val SELECT_IMAGE_REQUEST: Int = 1001
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setTitle(R.string.signup_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener(this)
        ivChooseImage.setOnClickListener(this)

        progress.visibility = View.GONE
    }


    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        if (menuItem?.getItemId() == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)

    }

    private fun signUp() {
        progress.visibility = View.VISIBLE
        btnSignUp.visibility = View.GONE
        firebaseAuth?.createUserWithEmailAndPassword(
            etEmailId.text.toString(),
            etPassword.text.toString()
        )
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth?.getCurrentUser()

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(etName.text.toString())
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                uploadImage(user)
                                //insertUpdateUser(user)
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed. Please try again",
                        Toast.LENGTH_SHORT
                    ).show();
                    progress.visibility = View.GONE
                    btnSignUp.visibility = View.VISIBLE
                }
            }
    }

    private fun uploadImage(user: FirebaseUser) {
        var file = File(filePath.toString())
        var storageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("/images/" + user.uid + file.name)
        storageRef.putFile(filePath!!)
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                    storageRef.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                        override fun onSuccess(p0: Uri?) {
                            insertUpdateUser(user, p0?.toString()!!)
                        }
                    })
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {

                }

            }).addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(p0: UploadTask.TaskSnapshot?) {

                }
            })
    }

    private fun insertUpdateUser(currentUser: FirebaseUser, profileUri: String) {
        val firebase =
            FirebaseDatabase.getInstance()?.reference?.child("users")?.child(currentUser.uid)
        firebase?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this@SignUpActivity, "Sign up successfully", Toast.LENGTH_SHORT)
                    .show();
                finish()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        var file = File(filePath.toString())
        var userData = User(
            currentUser.uid,
            currentUser.displayName.toString(),
            currentUser.email.toString(),
            false,
            profileUri
        )
        firebase?.setValue(userData)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSignUp -> {
                if (TextUtils.isEmpty(etName.text.toString().trim())) {
                    Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
                } else if (TextUtils.isEmpty(etEmailId.text.toString().trim())) {
                    Toast.makeText(this, "Please enter email id", Toast.LENGTH_SHORT).show()
                } else if (TextUtils.isEmpty(etPassword.text.toString().trim())) {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                } else if (etPassword.text.toString().trim().length < 8) {
                    Toast.makeText(
                        this,
                        "Minimum 8 character required for password",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (TextUtils.isEmpty(etConfirmPassword.text.toString().trim())) {
                    Toast.makeText(this, "Please enter confirm password", Toast.LENGTH_SHORT).show()
                } else if (!etPassword.text.toString().trim().equals(etConfirmPassword.text.toString().trim())) {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                } else {
                    if (filePath == null)
                        Toast.makeText(
                            this,
                            "Please select profile picture",
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        signUp()
                }
            }

            R.id.ivChooseImage -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        chooseImageIntent()
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_PERMISSION
                        )
                    }
                } else {
                    chooseImageIntent()
                }
            }
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageIntent()
            }
        }
    }

    var filePath: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.data

            try {
                var bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
                ivChooseImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
