package com.example.myapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityInsertFecesBinding
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap


class InsertFeces : AppCompatActivity() {

    class FecesSet{
        var names = arrayListOf<String>()
        var descriptions = arrayListOf<String>()
        val IDs = arrayListOf<Int>()
    }

    var GFecesID : Int = 0
    var GDay : Int = 0
    var GMonth : Int = 0
    var GYear : Int = 0
    var GHour : Int = 0
    var GMinute : Int = 0

    private lateinit var binding : ActivityInsertFecesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInsertFecesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myIntent = intent // gets the previously created intent

        GUserId = myIntent.getStringExtra("user_id").toString()
        val Vyear: Int = myIntent.getIntExtra("year", 0)
        val Vmonth: Int = myIntent.getIntExtra("month", 0)
        val Vday: Int = myIntent.getIntExtra("day", 0)

        val localDateTime: LocalDateTime = LocalDateTime.now()
        GDay = localDateTime.dayOfMonth
        GMonth = localDateTime.monthValue
        GYear = localDateTime.year
        GHour = localDateTime.hour
        GMinute = localDateTime.minute

        if(Vyear == 0) {
            val datePicker =
                DatePickerFragment { _, _, _ -> onDateSelected(Vday, Vmonth, Vyear) }
            datePicker.show(supportFragmentManager, "datePicker")
        }else{
            GDay = Vday
            GMonth = Vmonth
            GYear = Vyear
            val timePicker = TimePickerFragment { hour, minute -> onTimeSelected(hour, minute)}
            timePicker.show(supportFragmentManager, "timePicker")
        }

        updateSelectedDateTime()

        fillFeces()

        binding.buttonSelectDate.setOnClickListener(){
            val datePicker = DatePickerFragment {day, month, year -> onDateSelected(day, month, year)}
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.buttonSendForm.setOnClickListener(){
            createFeces()
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

    private fun createFeces(){
        val url = Utils.composeUrl(
            GUserId, "table/registered_feces")
        val queue = Volley.newRequestQueue(this)
        val localDateTime: LocalDateTime = LocalDateTime.of(GYear, GMonth, GDay, GHour, GMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        val params: MutableMap<String?, String?> = HashMap()
        params["user"] = GUserId
        params["feces"] = GFecesID.toString()
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

        val returnIntent = Intent()
        returnIntent.putExtra("return_data", 1)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun fillFeces(){
        var fecesSet : FecesSet
        val url = Utils.composeUrl(
            GUserId, "table/v_all_languages_feces")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
                fecesSet = parseJson(it)

                val adp1: ArrayAdapter<String> = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1, fecesSet.names
                )
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSelectFeces.adapter = adp1

                binding.spinnerSelectFeces.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        GFecesID = fecesSet.IDs.get(position)

                        binding.fecesDescription.text = fecesSet.descriptions.get(position)

                        val res = ResourcesCompat.getDrawable(resources,
                            when (position) {
                                0 -> R.drawable.type1_poop
                                1 -> R.drawable.type2_poop
                                2 -> R.drawable.type3_poop
                                3 -> R.drawable.type4_poop
                                4 -> R.drawable.type5_poop
                                5 -> R.drawable.type6_poop
                                6 -> R.drawable.type7_poop
                                else -> {R.drawable.ic_remove}
                            }, null)
                        binding.imageFeces.setImageDrawable(res)
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

    private fun parseJson(jsonObject: JSONObject): FecesSet {
        val ret = FecesSet()
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val feces = data.getJSONObject(it)
            ret.names.add(when (GLanguage) {
                Language.ENGLISH -> feces.get("name_en").toString()
                Language.SPANISH -> feces.get("name_es").toString()
            })
            ret.descriptions.add(when (GLanguage) {
                Language.ENGLISH -> feces.get("description_en").toString()
                Language.SPANISH -> feces.get("description_es").toString()
            })
            ret.IDs.add(feces.get("id").toString().toInt())
        }
        return ret
    }

    private fun updateSelectedDateTime(){
        val localDateTime: LocalDateTime = LocalDateTime.of(GYear, GMonth, GDay, GHour, GMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        binding.textDateTime.text = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(zonedDateTime)
    }
}