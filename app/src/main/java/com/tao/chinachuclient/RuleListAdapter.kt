package com.tao.chinachuclient

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import sugtao4423.library.chinachu4j.Rule

class RuleListAdapter(context: Context) : ArrayAdapter<Rule>(context, android.R.layout.simple_list_item_1) {

    private val mInflater = context.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val oldCategoryColor = (context.applicationContext as App).currentServer.oldCategoryColor

    private data class ViewHolder(
            val title: TextView,
            val channel: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = mInflater.inflate(R.layout.program_list_layout, parent, false)
            val title = view.findViewById<TextView>(R.id.programTitle)
            val channel = view.findViewById<TextView>(R.id.programDate)

            holder = ViewHolder(title, channel)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = getItem(position) ?: return view!!

        if (item.categories.isNotEmpty()) {
            view?.setBackgroundResource(
                    if (oldCategoryColor) {
                        when (item.categories[0]) {
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
                        when (item.categories[0]) {
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
                    })
        }

        val title = item.reserveTitles.let {
            if (it.isEmpty()) {
                "any"
            } else {
                it.joinToString()
            }
        }

        val channel = item.channels.let {
            if (it.isEmpty()) {
                "any"
            } else {
                it.joinToString()
            }
        }

        holder.title.text = title
        holder.channel.text = channel

        if (item.isDisabled) {
            holder.title.setTextColor(Color.GRAY)
            holder.channel.setTextColor(Color.GRAY)
            holder.title.paint.flags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.channel.paint.flags = holder.channel.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.titleText))
            holder.channel.setTextColor(ContextCompat.getColor(context, R.color.dateText))
            holder.title.paint.flags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.channel.paint.flags = holder.channel.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        return view!!
    }

    override fun addAll(rules: Array<Rule>) {
        rules.map {
            add(it)
        }
    }

}