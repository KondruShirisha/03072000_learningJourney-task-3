package com.example.myapplication_task3.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.myapplication_task3.R
import com.example.myapplication_task3.firebase.FireStoreClass
import com.example.myapplication_task3.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
private lateinit var  toolbarSignUp :Toolbar
private lateinit var usernameInputSignUp :TextInputEditText
private lateinit var emailInputSignUp :TextInputEditText
private lateinit var passwordInputSignUp :TextInputEditText
private lateinit var reenterPasswordInputSignUp :TextInputEditText
private lateinit var buttonSignUp:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        toolbarSignUp= findViewById(R.id.toolbar_sign_up_activity)
        usernameInputSignUp=findViewById(R.id.username_input_sign_up)
        emailInputSignUp=findViewById(R.id.email_input_sign_up)
        passwordInputSignUp=findViewById(R.id.password_input_sign_up)
        reenterPasswordInputSignUp=findViewById(R.id.reenter_password_input_sign_up)
        buttonSignUp=findViewById(R.id.btn_sign_up)

        setupActionBar()
        " ".also { title = it }

           buttonSignUp.setOnClickListener {
               registerUser()
           }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarSignUp)

        val actionBar= supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button)

        }
        toolbarSignUp.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to register a user to our app using the Firebase.
     * For more details visit: https://firebase.google.com/docs/auth/android/custom-auth
     */
    private fun registerUser() {
        val name: String = usernameInputSignUp.text.toString().trim() { it <= ' ' }
        val email: String = emailInputSignUp.text.toString().trim { it <= ' ' }
        val password: String = passwordInputSignUp.text.toString().trim { it <= ' ' }
        val reenterPassword: String = reenterPasswordInputSignUp.text.toString().trim { it <= ' ' }
        if (validateForm(name, email, password, reenterPassword)) {

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // If the registration is successfully done
                    if (task.isSuccessful) {

                        // Firebase registered user
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        // Registered Email
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid, name, registeredEmail, reenterPassword)

                        // call the registerUser function of FirestoreClass to make an entry in the database.
                        FireStoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm(name: String, email: String, password: String, ReEnter_Password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            TextUtils.isEmpty(ReEnter_Password) -> {
                showErrorSnackBar("Please re-enter password.")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this, " you have successfully registered registered" ,
            Toast.LENGTH_SHORT,
        ).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

}