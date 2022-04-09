package com.tao.chinachuclient

import android.content.Context
import android.graphics.Color
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.tao.chinachuclient.databinding.ProgramListLayoutBinding
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import java.text.SimpleDateFormat
import java.util.*

class ProgramListAdapter(context: Context, val type: Int) :
    ArrayAdapter<Any>(context, android.R.layout.simple_list_item_1) {

    private val oldCategoryColor =
        (context.applicationContext as App).currentServer.oldCategoryColor
    private val nowYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            val tBinding = ProgramListLayoutBinding.inflate(inflater, parent, false)
            tBinding.root.tag = tBinding
            tBinding
        } else {
            convertView.tag as ProgramListLayoutBinding
        }

        var item = getItem(position)
        item = when (type) {
            Type.RESERVES -> (item as Reserve).program
            Type.RECORDED -> (item as Recorded).program
            else -> item as Program
        }

        binding.root.setBackgroundResource(
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

        binding.programTitle.apply {
            text = item.title
            paint.isAntiAlias = true
        }
        binding.programDate.apply {
            text = getDateText(item)
            paint.isAntiAlias = true
        }

        if (type == Type.RESERVES) {
            val reserve = getItem(position) as Reserve
            if (!reserve.isManualReserved && reserve.isSkip) {
                binding.programTitle.let {
                    it.setTextColor(Color.GRAY)
                    it.paint.flags = it.paintFlags or STRIKE_THRU_TEXT_FLAG
                }
                binding.programDate.let {
                    it.setTextColor(Color.GRAY)
                    it.paint.flags = it.paintFlags or STRIKE_THRU_TEXT_FLAG
                }
            } else {
                binding.programTitle.let {
                    it.setTextColor(ContextCompat.getColor(context, R.color.titleText))
                    it.paint.flags = it.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
                }
                binding.programDate.let {
                    it.setTextColor(ContextCompat.getColor(context, R.color.dateText))
                    it.paint.flags = it.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
        return binding.root
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
