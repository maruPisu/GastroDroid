package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityInsertSymptomBinding
import org.json.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*



class InsertSymptom : AppCompatActivity() {

    class SymptomSet{
        var names = arrayListOf<String>()
        val IDs = arrayListOf<Int>()
    }

    var gSymptomID : Int = 0
    var gDay : Int = 0
    var gMonth : Int = 0
    var gYear : Int = 0
    var gHour : Int = 0
    var gMinute : Int = 0

    private lateinit var binding : ActivityInsertSymptomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInsertSymptomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myIntent = intent // gets the previously created intent

        GUserId = myIntent.getStringExtra("user_id").toString()
        val vYear: Int = myIntent.getIntExtra("year", 0)
        val vMonth: Int = myIntent.getIntExtra("month", 0)
        val vDay: Int = myIntent.getIntExtra("day", 0)

        val localDateTime: LocalDateTime = LocalDateTime.now()
        gDay = localDateTime.dayOfMonth
        gMonth = localDateTime.monthValue
        gYear = localDateTime.year
        gHour = localDateTime.hour
        gMinute = localDateTime.minute

        if(vYear == 0) {
            val datePicker =
                DatePickerFragment { _, _, _ -> onDateSelected(vDay, vMonth, vYear) }
            datePicker.show(supportFragmentManager, "datePicker")
        }else{
            gDay = vDay
            gMonth = vMonth
            gYear = vYear
            val timePicker = TimePickerFragment { hour, minute -> onTimeSelected(hour, minute)}
            timePicker.show(supportFragmentManager, "timePicker")
        }

        updateSelectedDateTime()

        fillSymptoms()

        binding.buttonSelectDate.setOnClickListener(){
            val datePicker = DatePickerFragment {day, month, year -> onDateSelected(day, month, year)}
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.buttonSendForm.setOnClickListener(){
            createSymptom()
        }
    }
    private fun onDateSelected(day:Int, month:Int, year:Int){
        gDay = day
        gMonth = month
        gYear = year
        val timePicker = TimePickerFragment { hour, minute -> onTimeSelected(hour, minute)}
        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun onTimeSelected(hour:Int, minute:Int){
        gHour = hour
        gMinute = minute
        updateSelectedDateTime()
    }

    private fun createSymptom(){
        val url = Utils.composeUrl(
            GUserId, "table/registered_symptom")
        val queue = Volley.newRequestQueue(this)
        val localDateTime: LocalDateTime = LocalDateTime.of(gYear, gMonth, gDay, gHour, gMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        val params: MutableMap<String?, String?> = HashMap()
        params["user"] = GUserId
        params["symptom"] = gSymptomID.toString()
        params["datetime"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .format(zonedDateTime) + "Z"
        val parameters = (params as Map<*, *>?)?.let { JSONObject(it) }

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, parameters,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
            }, Response.ErrorListener {
                Log.d("Mainactivity", getString(R.string.api_call_unsuccessful)+it.toString())
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
        finish()
    }

    private fun fillSymptoms(){
        var symptomSet : SymptomSet
        val url = Utils.composeUrl(
            GUserId, "table/v_all_languages_symptom")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
                symptomSet = parseJson(it)

                val adp1: ArrayAdapter<String> = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1, symptomSet.names
                )
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSelectSymptom.adapter = adp1

                binding.spinnerSelectSymptom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        gSymptomID = symptomSet.IDs.get(position)
                    }
                }
            }, Response.ErrorListener {
                Log.d("Mainactivity", getString(R.string.api_call_unsuccessful)+it.toString())
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

    private fun parseJson(jsonObject: JSONObject): SymptomSet {
        val ret = SymptomSet()
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val symptom = data.getJSONObject(it)
            ret.names.add(when (GLanguage) {
                Language.ENGLISH -> symptom.get("name_en").toString()
                Language.SPANISH -> symptom.get("name_es").toString()
            })
            ret.IDs.add(symptom.get("id").toString().toInt())
        }
        return ret
    }

    private fun updateSelectedDateTime(){
        val localDateTime: LocalDateTime = LocalDateTime.of(gYear, gMonth, gDay, gHour, gMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        binding.textDateTime.text = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(zonedDateTime)
    }
}