package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    class SymptomSet
        () {
        var names = arrayListOf<String>()
        var descriptions = arrayListOf<String>()
        val IDs = arrayListOf<Int>()
        /*
        dolor de cabeza
        dolor epigástrico
        dolor mesogástrico
        dolor hipogástrico
        gases
        pesadez de estomago
        saciedad precoz
        * */
    }

    var gSymptomID : Int = 0
    var gDay : Int = 0
    var gMonth : Int = 0
    var gYear : Int = 0
    var gHour : Int = 0
    var gMinute : Int = 0
    var gSymptomSet : SymptomSet = SymptomSet()

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
        binding.buttonSelectSymptom.setOnClickListener(){
            showPopupWindow(binding.buttonSelectSymptom, gSymptomSet.names)
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
        val url = Utils.composeUrl(
            GUserId, "table/v_all_languages_symptom")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
                gSymptomSet = parseJson(it)
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
            ret.descriptions.add(when (GLanguage) {
                Language.ENGLISH -> symptom.get("description_en").toString()
                Language.SPANISH -> symptom.get("description_es").toString()
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

    private fun showPopupWindow(view: View, items: List<String>) {
        // Inflate the pop-up window layout
        val popupView = LayoutInflater.from(view.context).inflate(R.layout.popup_window_layout, null)

        // Create the pop-up window
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Set up the ListView
        val listView = popupView.findViewById<ListView>(R.id.resultListView)
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        // Set up the search bar
        val searchEditText = popupView.findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        listView.setOnItemClickListener { parent, _, position, id ->
            gSymptomID = gSymptomSet.IDs[position]
            binding.textSymptom.text = gSymptomSet.names[position] //selectedItem.toString()
            binding.symptomDescription.text = gSymptomSet.descriptions[position]

            popupWindow.dismiss()
        }

        // Show the pop-up window
        binding.textSymptom.text = ""
        binding.symptomDescription.text = ""
        popupWindow.showAsDropDown(view)
    }
}