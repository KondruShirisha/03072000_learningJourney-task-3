package com.example.myapplication_task3.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.myapplication_task3.R
import com.example.myapplication_task3.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : BaseActivity() {
     private lateinit var auth: FirebaseAuth
    private lateinit var  toolbarLogin : Toolbar

    private lateinit var emailInputLogIn :TextInputEditText
    private lateinit var passwordInputLogIn : TextInputEditText
    private lateinit var btnLoginIn : Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInputLogIn =findViewById(R.id.email_input_log_in)
        passwordInputLogIn = findViewById(R.id.password_input_log_in)
        btnLoginIn=findViewById(R.id.btn_login_in)


        auth=FirebaseAuth.getInstance()  // create auth instance

        toolbarLogin= findViewById(R.id.toolbar_log_in_activity)
        setupActionBar()
        title = " "

        btnLoginIn.setOnClickListener {
            logInRegisterUser()
        }


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarLogin)

        val actionBar= supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button)

        }
        toolbarLogin.setNavigationOnClickListener { onBackPressed() }
    }

    private fun logInRegisterUser(){
        val email:String= emailInputLogIn.text.toString().trim { it <= ' ' }
        val password: String = passwordInputLogIn.text.toString().trim { it <= ' ' }

        if(validateForm(email, password)){
          //  showProgressDialog(resources.getString(R.string.please_wait))

            // Sign-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    //when getInstance is done with sign .hide progress bar
                  //  hideProgressDialog()
                    if (task.isSuccessful) {
                        // Calling the FirestoreClass signInUser function to get the data of user from database.
                       // FirestoreClass().loadUserData(this@SignInActivity)

                        Log.d("sign in","signInwithemail:success")

                        //after login clicked it should go to MyApp Activity
                        startActivity(Intent(this, MyAppActivity::class.java))



                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }



        }
    }


    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm( email: String, password: String, ): Boolean {
        return when {

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }
 // A function to get the user details from the firestore database after authentication.
    fun logInSuccess(user:User) {
        startActivity(Intent(this, MyAppActivity::class.java))
          finish()
    }
}