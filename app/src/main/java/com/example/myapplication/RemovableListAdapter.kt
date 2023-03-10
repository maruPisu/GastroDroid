package com.example.myapplication

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast


class RemovableListAdapter(val activity: Activity, val list:List<RemovableListData>,
                           private val listener: MyListAdapterListener):
    ArrayAdapter<RemovableListData>(activity,R.layout.removable_list_item) {
    override fun getCount(): Int {
        return list.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val context = activity.layoutInflater
        val rowView = context.inflate(R.layout.removable_list_item, null)

        val removableText = rowView.findViewById<TextView>(R.id.removable_text)

        removableText.text = list[position].value

        val imageview1: ImageView = rowView.findViewById(R.id.removable_image)
        imageview1.setOnClickListener {
            listener.onEventOccurred(position)
        }

        return rowView
    }
}