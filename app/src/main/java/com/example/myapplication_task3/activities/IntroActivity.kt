package com.example.myapplication_task3.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.myapplication_task3.R

class IntroActivity : BaseActivity() {
    private lateinit var btnSignUpIntro :Button
    private lateinit var btnLoginIntro : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

                btnLoginIntro= findViewById(R.id.btn_log_in_intro)
                btnLoginIntro.setOnClickListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                 btnSignUpIntro=findViewById(R.id.btn_sign_up_intro)
                 btnSignUpIntro.setOnClickListener {
                     startActivity(Intent(this, SignUpActivity::class.java))

                 }


    }
}