package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ArrayAdapter
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_symptom_calendar.*
import kotlinx.android.synthetic.main.activity_symptoms_in_day.*
import kotlinx.android.synthetic.main.activity_symptoms_in_day.floatingAddSymptom
import org.json.JSONObject
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap

var localDateTime = LocalDate.now()
var items = mutableListOf<String>()
var GUser : String = ""

class SymptomsInDay : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_in_day)
        val today : LocalDate = LocalDate.now()

        val myIntent = intent // gets the previously created intent

        GUser = myIntent.getStringExtra("user_id").toString()
        val year: Int = myIntent.getIntExtra("year", today.year)
        val month: Int = myIntent.getIntExtra("month", today.monthValue)
        val day: Int = myIntent.getIntExtra("day", today.dayOfMonth)

        localDateTime = LocalDate.of(year, month, day)

        val datetimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        symptoms_day_text.text = "Symptoms of " + localDateTime.format(datetimeFormatter)

        fillList()

        floatingAddSymptom.setOnClickListener(){
            val intent = Intent(this@SymptomsInDay, InsertSymptom::class.java).apply {}
            intent.putExtra("user_id",GUser);
            intent.putExtra("year",year);
            intent.putExtra("month",month);
            intent.putExtra("day",day);
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
            GUserId, "table/v_user_symptoms")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", "Api call successful ")
                parseJson(it)
            }, Response.ErrorListener {
                Log.d("Mainactivity", "Api call unsuccessful "+it.toString())
            }
        ){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val headers: MutableMap<String, String> = HashMap()
                headers["X-Session-Token"] = "abcd"
                //headers["bla"] = "abcd"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    private fun parseJson(jsonObject: JSONObject){
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val book = data.getJSONObject(it)
            val symptomName = book.get("s_name").toString()
            val userId = book.get("u_id").toString()
            val dateTime = LocalDateTime.parse(book.get("datetime").toString(), formatter)
            if (dateTime.toLocalDate() == localDateTime && userId == GUser){
                items.add(dateTime.toLocalTime().toString() + " - " + symptomName)
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        symptom_day_list.adapter = adapter
    }
}