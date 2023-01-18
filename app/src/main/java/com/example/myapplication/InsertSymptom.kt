package com.example.myapplication

import android.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_insert_symptom.*
import okhttp3.internal.Util
import org.json.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

var GSymptomID : Int = 0
var GDay : Int = 0
var GMonth : Int = 0
var GYear : Int = 0
var GHour : Int = 0
var GMinute : Int = 0
var GZoneOffset : Int = 0
var GUserId : String = ""

class SymptomSet{
    var names = arrayListOf<String>()
    val IDs = arrayListOf<Int>()
}

class InsertSymptom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.myapplication.R.layout.activity_insert_symptom)

        val myIntent = intent // gets the previously created intent

        GUserId = myIntent.getStringExtra("user_id").toString()
        val year: Int = myIntent.getIntExtra("year", 0)
        val month: Int = myIntent.getIntExtra("month", 0)
        val day: Int = myIntent.getIntExtra("day", 0)

        val localDateTime: LocalDateTime = LocalDateTime.now()
        GDay = localDateTime.dayOfMonth
        GMonth = localDateTime.monthValue
        GYear = localDateTime.year
        GHour = localDateTime.hour
        GMinute = localDateTime.minute

        if(year == 0) {
            val datePicker =
                DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
            datePicker.show(supportFragmentManager, "datePicker")
        }else{
            GDay = day
            GMonth = month
            GYear = year
            val timePicker = TimePickerFragment { hour, minute -> onTimeSelected(hour, minute)}
            timePicker.show(supportFragmentManager, "timePicker")
        }

        updateSelectedDateTime()

        fillSymptoms()

        buttonSelectDate.setOnClickListener(){
            val datePicker = DatePickerFragment {day, month, year -> onDateSelected(day, month, year)}
            datePicker.show(supportFragmentManager, "datePicker")
        }

        buttonSendForm.setOnClickListener(){
            createSymptom()
        }
    }
    private fun onDateSelected(day:Int, month:Int, year:Int){
        GDay = day
        GMonth = month
        GYear = year
        val timePicker = TimePickerFragment { hour, minute -> onTimeSelected(hour, minute)}
        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun onTimeSelected(hour:Int, minute:Int){
        GHour = hour
        GMinute = minute
        updateSelectedDateTime()
    }

    private fun createSymptom(){
        var symptomSet : SymptomSet
        val url = Utils.composeUrl(
            GUserId, "table/registered_symptom")
        val queue = Volley.newRequestQueue(this)
        val localDateTime: LocalDateTime = LocalDateTime.of(GYear, GMonth, GDay, GHour, GMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        val params: MutableMap<String?, String?> = HashMap()
        params["user"] = GUserId
        params["symptom"] = GSymptomID.toString()
        params["datetime"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .format(zonedDateTime) + "Z"
        val parameters = JSONObject(params as Map<*, *>?)

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, parameters,
            Response.Listener {
                Log.d("Mainactivity", "Api call successful ")
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
        finish()
    }

    private fun fillSymptoms(){
        var symptomSet : SymptomSet
        val url = Utils.composeUrl(
            GUserId, "table/symptom")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", "Api call successful ")
                symptomSet = parseJson(it)

                val adp1: ArrayAdapter<String> = ArrayAdapter<String>(
                    this,
                    R.layout.simple_list_item_1, symptomSet.names
                )
                adp1.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spinner_select_symptom.adapter = adp1

                spinner_select_symptom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        GSymptomID = symptomSet.IDs.get(position)
                    }
                }
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

    private fun parseJson(jsonObject: JSONObject): SymptomSet {
        var ret : SymptomSet = SymptomSet()
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val book = data.getJSONObject(it)
            ret.names.add(book.get("name").toString())
            ret.IDs.add(book.get("id").toString().toInt())
        }
        return ret
    }

    private fun updateSelectedDateTime(){
        val localDateTime: LocalDateTime = LocalDateTime.of(GYear, GMonth, GDay, GHour, GMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        text_date_time.text = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(zonedDateTime)
    }
}