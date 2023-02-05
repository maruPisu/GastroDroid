package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_symptom_calendar.*
import org.json.JSONObject
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class EventCalendar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        val myIntent = intent // gets the previously created intent
        GUserId = myIntent.getStringExtra("user_id").toString()

        symptomCalendarView.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(view: View?, date: DateData) {
                val intent = Intent(this@EventCalendar, SymptomsInDay::class.java).apply {}
                intent.putExtra("user_id",GUserId)
                intent.putExtra("year",date.year)
                intent.putExtra("month",date.month)
                intent.putExtra("day",date.day)
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
        val url = Utils.composeUrl(
            GUserId, "table/v_user_anything")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", "Api call successful ")
                parseJson(it)
            }, Response.ErrorListener {
                Log.d("Mainactivity", "Api call unsuccessful "+it.toString())
            }
        ){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["X-Session-Token"] = "abcd"
                //headers["bla"] = "abcd"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    private fun parseJson(jsonObject: JSONObject){
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        symptomCalendarView.markedDates.all.clear()
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val book = data.getJSONObject(it)
            Log.d("a", book.get("date").toString())
            val date = LocalDate.parse(book.get("date").toString(), formatter)
            symptomCalendarView.markDate(
                DateData(date.year, date.monthValue, date.dayOfMonth).setMarkStyle(
                    MarkStyle(MarkStyle.DOT, Color.GREEN)
                )
            )
        }
    }
}