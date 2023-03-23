package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityInsertMealBinding
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AllergenSet{
    var names = arrayListOf<String>()
    val IDs = arrayListOf<Int>()
}
interface MyListAdapterListener {
    fun onEventOccurred(position: Int)
}

class InsertMeal : AppCompatActivity() , MyListAdapterListener{

    val allergenSet = AllergenSet()
    var gDay : Int = 0
    var gMonth : Int = 0
    var gYear : Int = 0
    var gHour : Int = 0
    var gMinute : Int = 0
    var gUserId : String = ""
    private var userIsInteracting = false
    private var mealId: Int = 0

    var removableList = mutableListOf<RemovableListData>()

    private lateinit var binding: ActivityInsertMealBinding
    private lateinit var removableListAdapter : RemovableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInsertMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myIntent = intent // gets the previously created intent

        allergenSet.names.add(" --- ")  // This is to avoid the automatic selection of a useful item
        allergenSet.IDs.add(-1)  // This is to avoid the automatic selection of a useful item

        gUserId = myIntent.getStringExtra("user_id").toString()
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

        binding.spinnerSelectMeal.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                if (position != 0) {
                    if(!removableListContains(allergenSet.IDs[position]))
                        removableList.add(
                            RemovableListData(
                                allergenSet.IDs[position],
                                allergenSet.names[position]
                            )
                        )
                    removableListAdapter.notifyDataSetChanged()
                    binding.spinnerSelectMeal.setSelection(0)
                }
            }
        }

        removableListAdapter = RemovableListAdapter(this, removableList, this)

        val listView = binding.listAllergen
        listView.adapter = removableListAdapter

        updateSelectedDateTime()

        fillAllergens()

        binding.buttonSelectDate.setOnClickListener(){
            val datePicker = DatePickerFragment {day, month, year -> onDateSelected(day, month, year)}
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.buttonSendForm.setOnClickListener(){
            if(removableList.size > 0){
                createMeal()
            }else{
                Toast.makeText(applicationContext, getString(R.string.warning_empty_meal), Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    private fun removableListContains(id: Int): Boolean{
        for (element in removableList){
            if(id == element.id){
                return true
            }
        }
        return false
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

    private fun createAllAllergensInMeal(){
        for (element in removableList){
            createAllergenInMeal(element)
        }
        finish()
    }

    private fun createAllergenInMeal(data: RemovableListData){
        val url = Utils.composeUrl(
            gUserId, "table/allergen_in_meal")
        val queue = Volley.newRequestQueue(this)

        val params: MutableMap<String?, String?> = HashMap()
        params["user"] = gUserId
        params["meal"] = mealId.toString()
        params["allergen"] = data.id.toString()
        val parameters = (params as Map<*, *>?)?.let { JSONObject(it) }

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, parameters,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful)+it.toString())
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

    private fun createMeal(){
        val url = Utils.composeUrl(
            gUserId, "table/registered_meal")
        val queue = Volley.newRequestQueue(this)
        val localDateTime: LocalDateTime = LocalDateTime.of(gYear, gMonth, gDay, gHour, gMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        val params: MutableMap<String?, String?> = HashMap()
        params["user"] = gUserId
        params["datetime"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .format(zonedDateTime) + "Z"
        val parameters = (params as Map<*, *>?)?.let { JSONObject(it) }

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, parameters,
            Response.Listener {
                parseCreateMealJson(it)
                createAllAllergensInMeal()
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

    private fun fillAllergens(){
        val url = Utils.composeUrl(
            GUserId, "table/v_all_languages_allergen")
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener {

                parseFillAllergenJson(it)
                val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1, allergenSet.names
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSelectMeal.adapter = spinnerAdapter
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

    private fun parseCreateMealJson(jsonObject: JSONObject) {
        val data = jsonObject.getJSONObject("data")
        mealId = data.get("last_id").toString().toInt()
    }

    private fun parseFillAllergenJson(jsonObject: JSONObject) {
        val data = jsonObject.getJSONArray("data")
        (0 until data.length()).forEach {
            val allergen = data.getJSONObject(it)
            allergenSet.IDs.add(allergen.get("id").toString().toInt())
            allergenSet.names.add(when (GLanguage) {
                Language.ENGLISH -> allergen.get("name_en").toString()
                Language.SPANISH -> allergen.get("name_es").toString()
            })
        }
    }

    private fun updateSelectedDateTime(){
        val localDateTime: LocalDateTime = LocalDateTime.of(gYear, gMonth, gDay, gHour, gMinute)
        val zonedDateTime: ZonedDateTime =
            ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

        binding.textDateTime.text = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(zonedDateTime)
    }

    override fun onEventOccurred(position: Int) {
        removableList.removeAt(position)
        removableListAdapter.notifyDataSetChanged()
    }
}