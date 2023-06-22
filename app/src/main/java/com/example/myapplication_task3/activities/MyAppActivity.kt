package com.example.myapplication_task3.activities


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.example.myapplication_task3.R

class MyAppActivity : BaseActivity() {

    private lateinit var btnViewGallery : Button
    private lateinit var btnViewLatestLocation : Button
    private lateinit var toolbarMyapp:Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_app_activity)

        btnViewGallery=findViewById(R.id.btn_view_gallery)
        btnViewLatestLocation=findViewById(R.id.btn_view_latest_location)
        toolbarMyapp=findViewById(R.id.toolbar_my_app_activity)
        setupActionBar()
        " ".also { title = it }
        btnViewGallery.setOnClickListener {
            startActivity(
                Intent(
                    this@MyAppActivity,
                    GalleryActivity::class.java
                )
            )
        }
        btnViewLatestLocation.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarMyapp)

        val actionBar= supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_backbutton_black)
        }
        toolbarMyapp.setNavigationOnClickListener { onBackPressed() }
    }


}