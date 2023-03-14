package com.example.myapplication

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class TwoItemsListAdapter(val activity: Activity, val list:Events):
    ArrayAdapter<RemovableListData>(activity,R.layout.removable_list_item) {
    override fun getCount(): Int {
        return list.IDs.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val context = activity.layoutInflater
        val rowView = context.inflate(R.layout.event_list_item, parent, false)

        val text1 = rowView.findViewById<TextView>(R.id.event_text1)
        val text2 = rowView.findViewById<TextView>(R.id.event_text2)

        text1.text = list.types[position]
        text1.setTextColor(
            when (list.tables[position]) {
                "registered_feces" -> Color.parseColor("#0c04b0")
                "registered_meal" -> Color.parseColor("#1e8203")
                "registered_symptom" -> Color.parseColor("#910410")
                "registered_supplement" -> Color.parseColor("#BA1212")
                else -> { // Note the block
                    Color.parseColor("#FF0000")
                }
            } )
        text2.text = list.descriptions[position]

        return rowView
    }
}