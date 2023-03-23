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
import com.example.myapplication.databinding.ActivityEventCalendarBinding
import org.json.JSONObject
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthChangeListener
import sun.bob.mcalendarview.vo.DateData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventCalendar : AppCompatActivity() {

    private lateinit var binding : ActivityEventCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myIntent = intent // gets the previously created intent
        GUserId = myIntent.getStringExtra("user_id").toString()

        val current = LocalDateTime.now()
        updateMonth(current.year, current.monthValue)

        binding.eventCalendarView.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(view: View?, date: DateData) {
                val intent = Intent(this@EventCalendar, EventsInDay::class.java).apply {}
                intent.putExtra("user_id",GUserId)
                intent.putExtra("year",date.year)
                intent.putExtra("month",date.month)
                intent.putExtra("day",date.day)
                startActivity(intent)
            }
        })
        binding.eventCalendarView.setOnMonthChangeListener(object: OnMonthChangeListener() {
            override fun onMonthChange(year: Int, month: Int) {
                updateMonth(year, month)
            }
        })

        fillCalendar()
        //handle date click in calendar (new activity?)
    }
    override fun onResume() {
        super.onResume()
        fillCalendar()
    }

    private fun updateMonth(year: Int, month: Int){
        binding.eventTextDay.setText(buildString {
        append(Utils.getMonthName(month))
        append(" ")
        append(year)
    })
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
        binding.eventCalendarView.markedDates.all.clear()
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val book = data.getJSONObject(it)
            Log.d("a", book.get("date").toString())
            val date = LocalDate.parse(book.get("date").toString(), formatter)
            binding.eventCalendarView.markDate(
                DateData(date.year, date.monthValue, date.dayOfMonth).setMarkStyle(
                    MarkStyle(MarkStyle.DOT, Color.GREEN)
                )
            )
        }
    }
}