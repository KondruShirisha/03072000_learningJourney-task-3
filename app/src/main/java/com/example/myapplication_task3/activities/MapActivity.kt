package com.example.myapplication_task3.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.myapplication_task3.R

class MapActivity : AppCompatActivity() {
    private lateinit var toolbarMap:Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        toolbarMap=findViewById(R.id.toolbar_map_activity)

        setupActionBar()
        "".also { title = it }

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbarMap)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_backbutton_black)

        }
        toolbarMap.setNavigationOnClickListener { onBackPressed() }
    }


}