package com.tao.chinachuclient

import Chinachu4j.Program
import Chinachu4j.Recorded
import Chinachu4j.Reserve
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView

class ProgramListClickListener(private val context: Context, private val type: Int) : AdapterView.OnItemClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) {
            return
        }

        val i = Intent(context, ProgramDetail::class.java)
        when (type) {
            Type.RESERVES -> i.putExtra("reserve", (parent.getItemAtPosition(position) as Reserve))
            Type.RECORDED -> i.putExtra("recorded", (parent.getItemAtPosition(position) as Recorded))
            else -> i.putExtra("program", (parent.getItemAtPosition(position) as Program))
        }
        i.putExtra("type", type)
        context.startActivity(i)
    }

}