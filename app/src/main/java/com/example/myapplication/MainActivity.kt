package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIntent = intent // gets the previously created intent

        val name = myIntent.getStringExtra("name")
        val photoUrl = myIntent.getStringExtra("photo")
        val userId = myIntent.getStringExtra("id")
        textViewWelcome.text = name
        if (photoUrl != null) {
            if(photoUrl.isNotEmpty()) {
                Picasso.get().load(photoUrl).into(avatar)
            }
        }

        buttonEventCalendar.setOnClickListener(){
            val intent = Intent(this, EventCalendar::class.java).apply {}
            intent.putExtra("user_id",userId)
            startActivity(intent)
        }
    }
}