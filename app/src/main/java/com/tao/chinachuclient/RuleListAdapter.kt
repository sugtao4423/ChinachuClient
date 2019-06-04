package com.tao.chinachuclient

import sugtao4423.library.chinachu4j.Rule
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

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
            val title = view.findViewById<TextView>(R.id.program_title)
            val channel = view.findViewById<TextView>(R.id.program_date)

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

        return view!!
    }

    override fun addAll(rules: Array<Rule>) {
        rules.map {
            add(it)
        }
    }

}