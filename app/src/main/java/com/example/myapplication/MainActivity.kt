package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import java.util.*

enum class Language {
    ENGLISH, SPANISH
}

var GUserId : String = ""
var GLanguage: Language = Language.ENGLISH

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myIntent = intent // gets the previously created intent

        val name = myIntent.getStringExtra("name")
        val photoUrl = myIntent.getStringExtra("photo")
        val userId = myIntent.getStringExtra("id")
        binding.textViewWelcome.text = name
        if (photoUrl != null) {
            if(photoUrl.isNotEmpty()) {
                Picasso.get().load(photoUrl).into(binding.avatar)
            }
        }

        binding.buttonEventCalendar.setOnClickListener(){
            val intent = Intent(this, EventCalendar::class.java).apply {}
            intent.putExtra("user_id",userId)
            startActivity(intent)
        }

        val language = Locale.getDefault().language
        if (language == "es") {
            GLanguage = Language.SPANISH
        }
    }
}