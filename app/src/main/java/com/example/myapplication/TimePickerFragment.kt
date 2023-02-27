package com.example.myapplication

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDateTime

class TimePickerFragment(val listener: (hour:Int, minute:Int) -> Unit) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener  {

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        listener(hour, minute)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val localDateTime: LocalDateTime = LocalDateTime.now()
        val hour: Int = localDateTime.hour
        val minute: Int = localDateTime.minute

        val picker = TimePickerDialog(activity as Context, this, hour, minute, true)
        return picker
    }
}