package com.firebasechatkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import android.text.TextUtils
import android.view.MenuItem
import com.google.firebase.auth.UserProfileChangeRequest


class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setTitle(R.string.signup_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener(this)

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
        firebaseAuth?.createUserWithEmailAndPassword(etEmailId.text.toString(), etPassword.text.toString())
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth?.getCurrentUser()

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(etName.text.toString())
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(this, "Authentication failed. Please try again", Toast.LENGTH_SHORT).show();
                    progress.visibility = View.GONE
                    btnSignUp.visibility = View.VISIBLE
                }
            }
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
                    Toast.makeText(this, "Minimum 8 character required for password", Toast.LENGTH_SHORT).show()
                } else if (TextUtils.isEmpty(etConfirmPassword.text.toString().trim())) {
                    Toast.makeText(this, "Please enter confirm password", Toast.LENGTH_SHORT).show()
                } else if (!etPassword.text.toString().trim().equals(etConfirmPassword.text.toString().trim())) {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                } else {
                    signUp()
                }

            }
        }
    }

}
