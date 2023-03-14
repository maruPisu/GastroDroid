package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityEventsInDayBinding
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.content.Context
import android.view.View
import android.widget.TextView


class Events{
    fun clear(){
        types.clear()
        descriptions.clear()
        IDs.clear()
        tables.clear()
    }

    var types = arrayListOf<String>()
    var descriptions = arrayListOf<String>()
    val tables = arrayListOf<String>()
    val IDs = arrayListOf<Int>()
}

class EventsInDay : AppCompatActivity() {

    var localDateTime: LocalDate = LocalDate.now()
    private var items = Events()
    private var gUser : String = ""
    private lateinit var binding : ActivityEventsInDayBinding
    private lateinit var twoItemsListAdapter: TwoItemsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsInDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val today : LocalDate = LocalDate.now()

        val myIntent = intent // gets the previously created intent

        gUser = myIntent.getStringExtra("user_id").toString()
        val year: Int = myIntent.getIntExtra("year", today.year)
        val month: Int = myIntent.getIntExtra("month", today.monthValue)
        val day: Int = myIntent.getIntExtra("day", today.dayOfMonth)

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)

        localDateTime = LocalDate.of(year, month, day)

        val datetimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        binding.eventDayText.text = getString(R.string.events_of_day, localDateTime.format(datetimeFormatter))

        twoItemsListAdapter = TwoItemsListAdapter(this, items)
        binding.eventDayList.adapter = twoItemsListAdapter


        fillList()

        binding.eventDayList.setOnItemClickListener{ parent, _, position, _ ->
            alertDialog.setTitle(getString(R.string.delete_entry))
            alertDialog.setMessage(getString(R.string.delete_entry_confirmation))
            alertDialog.setPositiveButton(
                "yes"
            ) { _, _ ->
                deleteElement(items.tables[position], items.IDs[position])
            }
            alertDialog.setNegativeButton(
                "No"
            ) { _, _ ->}
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }

        binding.floatingAddSymptom.setOnClickListener(){
            val intent = Intent(this@EventsInDay, InsertSymptom::class.java).apply {}
            intent.putExtra("user_id",gUser)
            intent.putExtra("year",year)
            intent.putExtra("month",month)
            intent.putExtra("day",day)
            startActivity(intent)
        }

        binding.floatingAddMeal.setOnClickListener(){
            val intent = Intent(this@EventsInDay, InsertMeal::class.java).apply {}
            intent.putExtra("user_id",gUser)
            intent.putExtra("year",year)
            intent.putExtra("month",month)
            intent.putExtra("day",day)
            startActivity(intent)
        }

        binding.floatingAddFeces.setOnClickListener(){
            val intent = Intent(this@EventsInDay, InsertFeces::class.java).apply {}
            intent.putExtra("user_id",gUser)
            intent.putExtra("year",year)
            intent.putExtra("month",month)
            intent.putExtra("day",day)
            startActivity(intent)
        }

        binding.floatingAddSupplement.setOnClickListener(){
            val intent = Intent(this@EventsInDay, InsertSupplement::class.java).apply {}
            intent.putExtra("user_id",gUser)
            intent.putExtra("year",year)
            intent.putExtra("month",month)
            intent.putExtra("day",day)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.floatingActionsMenu.collapse()
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
            val event = data.getJSONObject(it)
            val value = event.get("value").toString()
            val table = event.get("table").toString()
            val type = when (GLanguage) {
                Language.ENGLISH -> event.get("type-en").toString()
                Language.SPANISH -> event.get("type-es").toString()
            }
            val id = event.get("id").toString().toInt()
            val date = LocalDate.parse(event.get("date").toString(), dateFormatter)
            val time = LocalTime.parse(event.get("time").toString(), timeFormatter)
            if (date == localDateTime){
                items.types.add(type)
                items.descriptions.add("$time - $value")
                items.IDs.add(id)
                items.tables.add(table)
            }
        }
        twoItemsListAdapter.notifyDataSetChanged()
    }

    private fun deleteElement(table: String, itemId: Int){
        val url = Utils.composeUrl(
            GUserId, "table/$table/$itemId"
        )
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.DELETE, url, null,
            Response.Listener {
                Log.d("Mainactivity", getString(R.string.api_call_successful))
                fillList()
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
}