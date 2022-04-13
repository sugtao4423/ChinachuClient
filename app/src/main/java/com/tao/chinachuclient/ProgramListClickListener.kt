package com.tao.chinachuclient

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import com.tao.chinachuclient.ui.programdetail.ProgramDetailActivity
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve

class ProgramListClickListener(private val context: Context, private val type: Int) :
    AdapterView.OnItemClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null) {
            return
        }

        val i = Intent(context, ProgramDetailActivity::class.java)
        when (type) {
            Type.RESERVES -> {
                i.putExtra("reserve", (parent.getItemAtPosition(position) as Reserve))
            }
            Type.RECORDED -> {
                i.putExtra("recorded", (parent.getItemAtPosition(position) as Recorded))
            }
            else -> {
                i.putExtra("program", (parent.getItemAtPosition(position) as Program))
            }
        }
        i.putExtra("type", type)
        context.startActivity(i)
    }

}
