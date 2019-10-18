package com.tao.chinachuclient

import android.content.Context
import android.graphics.Color
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import java.text.SimpleDateFormat
import java.util.*

class ProgramListAdapter(context: Context, val type: Int) :
        ArrayAdapter<Any>(context, android.R.layout.simple_list_item_1) {

    private val mInflater = context.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val oldCategoryColor = (context.applicationContext as App).currentServer.oldCategoryColor
    private val nowYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    private data class ViewHolder(
            val title: TextView,
            val date: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = mInflater.inflate(R.layout.program_list_layout, parent, false)
            val title = view.findViewById<TextView>(R.id.program_title)
            val date = view.findViewById<TextView>(R.id.program_date)

            holder = ViewHolder(title, date)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        var item = getItem(position) ?: return view!!
        item = when (type) {
            Type.RESERVES -> (item as Reserve).program
            Type.RECORDED -> (item as Recorded).program
            else -> item as Program
        }

        view?.setBackgroundResource(
                if (oldCategoryColor) {
                    when (item.category) {
                        "anime" -> R.drawable.old_anime
                        "information" -> R.drawable.old_information
                        "news" -> R.drawable.old_news
                        "sports" -> R.drawable.old_sports
                        "variety" -> R.drawable.old_variety
                        "drama" -> R.drawable.old_drama
                        "music" -> R.drawable.old_music
                        "cinema" -> R.drawable.old_cinema
                        "etc" -> R.drawable.old_etc
                        else -> R.drawable.old_etc
                    }
                } else {
                    when (item.category) {
                        "anime" -> R.drawable.anime
                        "information" -> R.drawable.information
                        "news" -> R.drawable.news
                        "sports" -> R.drawable.sports
                        "variety" -> R.drawable.variety
                        "drama" -> R.drawable.drama
                        "music" -> R.drawable.music
                        "cinema" -> R.drawable.cinema
                        "etc" -> R.drawable.etc
                        else -> R.drawable.etc
                    }
                }
        )

        holder.title.text = item.title
        holder.date.text = getDateText(item)

        val titlePaint = holder.title.paint
        val datePaint = holder.date.paint
        titlePaint.isAntiAlias = true
        datePaint.isAntiAlias = true

        if (type == Type.RESERVES) {
            val reserve = getItem(position) as Reserve
            if (!reserve.isManualReserved && reserve.isSkip) {
                holder.title.setTextColor(Color.GRAY)
                holder.date.setTextColor(Color.GRAY)
                titlePaint.flags = holder.title.paintFlags or STRIKE_THRU_TEXT_FLAG
                datePaint.flags = holder.date.paintFlags or STRIKE_THRU_TEXT_FLAG
            } else {
                holder.title.setTextColor(Color.parseColor(context.getString(R.color.titleText)))
                holder.date.setTextColor(Color.parseColor(context.getString(R.color.dateText)))
                titlePaint.flags = holder.title.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
                datePaint.flags = holder.date.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
        return view!!
    }

    private fun getDateText(item: Program): String {
        val dateText = run {
            val dateFormat = SimpleDateFormat("yyyy,MM/dd (E),HH:mm", Locale.JAPANESE)
            val startDates = dateFormat.format(Date(item.start)).split(",")
            val endDates = dateFormat.format(Date(item.end)).split(",")

            val startText = startDates.let {
                when {
                    it[0] != nowYear -> "${it[0]}/${it[1]} ${it[2]}"
                    else -> "${it[1]} ${it[2]}"
                }
            }
            val endText = endDates.let {
                when {
                    it[0] != startDates[0] -> "${it[0]}/${it[1]} ${it[2]}"
                    it[1] == startDates[1] -> it[2]
                    else -> "${it[1]} ${it[2]}"
                }
            }
            "$startText 〜 $endText"
        }

        if (type == Type.RESERVES || type == Type.RECORDING) {
            var deltaSec = ((item.start - System.currentTimeMillis()) / 1000).toInt()
            val suffix: String
            if (deltaSec < 0) {
                deltaSec = -deltaSec
                suffix = "前"
            } else {
                suffix = "後"
            }

            return if (deltaSec < 60) {
                "$dateText [%d秒%s]".format(deltaSec, suffix)
            } else {
                var delta = deltaSec.toFloat() / 60
                if (delta < 60.0f) {
                    "$dateText [%.1f分%s]".format(delta, suffix)
                } else {
                    delta /= 60f
                    if (delta < 24.0f) {
                        "$dateText [%.1f時間%s]".format(delta, suffix)
                    } else {
                        delta /= 24f
                        "$dateText [%.1f日%s]".format(delta, suffix)
                    }
                }
            }
        }

        return dateText
    }

    override fun addAll(objects: Array<*>) {
        objects.map {
            add(it)
        }
    }

}