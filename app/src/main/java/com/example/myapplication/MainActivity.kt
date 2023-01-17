package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myIntent = intent // gets the previously created intent

        val name = myIntent.getStringExtra("name")
        val email = myIntent.getStringExtra("email")
        val photoUrl = myIntent.getStringExtra("photoUrl")
        val userId = myIntent.getStringExtra("id")
        Toast.makeText(applicationContext,userId, Toast.LENGTH_SHORT).show()
        textViewWelcome.text = "Welcome " + name
        if (photoUrl != null) {
            if(photoUrl.isNotEmpty()) {
                Picasso.get().load(myIntent.getStringExtra("photo")).into(avatar);
            }
        }

        buttonSymptomCalendar.setOnClickListener(){
            val intent = Intent(this, SymptomCalendar::class.java).apply {}
            intent.putExtra("user_id",userId);
            startActivity(intent)
        }
    }
}