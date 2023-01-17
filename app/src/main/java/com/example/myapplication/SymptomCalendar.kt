package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_symptom_calendar.*
import org.json.JSONObject
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SymptomCalendar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        floatingAddSymptom.setOnClickListener(){
            val intent = Intent(this, InsertSymptom::class.java).apply {}
            intent.putExtra("user_id",1);
            startActivity(intent)
        }

        symptomCalendarView.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(view: View?, date: DateData) {
                val intent = Intent(this@SymptomCalendar, SymptomsInDay::class.java).apply {}
                intent.putExtra("user_id","1");
                intent.putExtra("year",date.year);
                intent.putExtra("month",date.month);
                intent.putExtra("day",date.day);
                startActivity(intent)
            }
        })

        fillCalendar()
        //handle date click in calendar (new activity?)
    }
    override fun onResume() {
        super.onResume()
        fillCalendar()
    }

    private fun fillCalendar(){
        var symptomSet : SymptomSet
        val url = "http://marupeace.com/goapi/table/v_user_symptoms"
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
            val dateTime = LocalDateTime.parse(book.get("datetime").toString(), formatter)
            symptomCalendarView.markDate(
                DateData(dateTime.year, dateTime.monthValue, dateTime.dayOfMonth).setMarkStyle(MarkStyle(MarkStyle.DOT, Color.GREEN))
            )

        }
    }
}