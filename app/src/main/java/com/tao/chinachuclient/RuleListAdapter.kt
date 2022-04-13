package com.tao.chinachuclient

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.tao.chinachuclient.databinding.ProgramListLayoutBinding
import sugtao4423.library.chinachu4j.Rule

class RuleListAdapter(context: Context) :
    ArrayAdapter<Rule>(context, android.R.layout.simple_list_item_1) {

    private val oldCategoryColor =
        (context.applicationContext as App).currentServer.oldCategoryColor

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            val tBinding = ProgramListLayoutBinding.inflate(inflater, parent, false)
            tBinding.root.tag = tBinding
            tBinding
        } else {
            convertView.tag as ProgramListLayoutBinding
        }

        val item = getItem(position) ?: return binding.root

        if (item.categories.isNotEmpty()) {
            binding.root.setBackgroundResource(
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
                }
            )
        }

        val title = item.reserveTitles.let { if (it.isEmpty()) "any" else it.joinToString() }
        val channel = item.channels.let { if (it.isEmpty()) "any" else it.joinToString() }
        binding.programTitle.text = title
        binding.programDate.text = channel

        if (item.isDisabled) {
            binding.programTitle.let {
                it.setTextColor(Color.GRAY)
                it.paint.flags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            binding.programDate.let {
                it.setTextColor(Color.GRAY)
                it.paint.flags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        } else {
            binding.programTitle.let {
                it.setTextColor(ContextCompat.getColor(context, R.color.titleText))
                it.paint.flags = it.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            binding.programDate.let {
                it.setTextColor(ContextCompat.getColor(context, R.color.dateText))
                it.paint.flags = it.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        return binding.root
    }

    override fun addAll(rules: Array<Rule>) {
        rules.map {
            add(it)
        }
    }

}
