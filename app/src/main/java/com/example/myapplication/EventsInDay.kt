package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityEventsInDayBinding
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.HashMap

var localDateTime: LocalDate = LocalDate.now()
var items = mutableListOf<String>()
var GUser : String = ""

class EventsInDay : AppCompatActivity() {

    private lateinit var binding : ActivityEventsInDayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsInDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val today : LocalDate = LocalDate.now()

        val myIntent = intent // gets the previously created intent

        GUser = myIntent.getStringExtra("user_id").toString()
        val year: Int = myIntent.getIntExtra("year", today.year)
        val month: Int = myIntent.getIntExtra("month", today.monthValue)
        val day: Int = myIntent.getIntExtra("day", today.dayOfMonth)

        localDateTime = LocalDate.of(year, month, day)

        val datetimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        binding.eventDayText.text = getString(R.string.events_of_day, localDateTime.format(datetimeFormatter))

        fillList()

        binding.floatingAddSymptom.setOnClickListener(){
            val intent = Intent(this@EventsInDay, InsertSymptom::class.java).apply {}
            intent.putExtra("user_id",GUser)
            intent.putExtra("year",year)
            intent.putExtra("month",month)
            intent.putExtra("day",day)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        fillList()
    }

    private fun fillList(){
        items.clear()
        val url = Utils.composeUrl(
            GUserId, "table/v_all_entries")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
                parseJson(it)
            }, Response.ErrorListener {
                Log.d("Mainactivity", getString(R.string.api_call_unsuccessful)+it.toString())
            }
        ){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["X-Session-Token"] = "abcd"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    private fun parseJson(jsonObject: JSONObject){
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val book = data.getJSONObject(it)
            val value = book.get("value").toString()
            val date = LocalDate.parse(book.get("date").toString(), dateFormatter)
            val time = LocalTime.parse(book.get("time").toString(), timeFormatter)
            if (date == localDateTime){
                items.add("$time - $value")
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        binding.eventDayList.adapter = adapter
    }
}