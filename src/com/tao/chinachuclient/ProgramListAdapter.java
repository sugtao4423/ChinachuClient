package com.tao.chinachuclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Chinachu4j.Program;
import Chinachu4j.Recorded;
import Chinachu4j.Reserve;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProgramListAdapter extends ArrayAdapter<Object>{
	private LayoutInflater mInflater;
	private boolean oldCategoryColor;
	private int type;

	public ProgramListAdapter(Context context, int type){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		oldCategoryColor = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("oldCategoryColor", false);
		this.type = type;
	}

	class ViewHolder{
		TextView title, date;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		Program item;
		if(type == Type.RESERVES)
			item = ((Reserve)getItem(position)).getProgram();
		else if(type == Type.RECORDED)
			item = ((Recorded)getItem(position)).getProgram();
		else
			item = (Program)getItem(position);

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.program_list_layout, null);
			TextView title = (TextView)convertView.findViewById(R.id.program_title);
			TextView date = (TextView)convertView.findViewById(R.id.program_date);

			holder = new ViewHolder();
			holder.title = title;
			holder.date = date;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		String category = item.getCategory();
		if(oldCategoryColor){
			switch(category){
			case "anime":
				convertView.setBackgroundResource(R.drawable.old_anime);
				break;
			case "information":
				convertView.setBackgroundResource(R.drawable.old_information);
				break;
			case "news":
				convertView.setBackgroundResource(R.drawable.old_news);
				break;
			case "sports":
				convertView.setBackgroundResource(R.drawable.old_sports);
				break;
			case "variety":
				convertView.setBackgroundResource(R.drawable.old_variety);
				break;
			case "drama":
				convertView.setBackgroundResource(R.drawable.old_drama);
				break;
			case "music":
				convertView.setBackgroundResource(R.drawable.old_music);
				break;
			case "cinema":
				convertView.setBackgroundResource(R.drawable.old_cinema);
				break;
			case "etc":
				convertView.setBackgroundResource(R.drawable.old_etc);
				break;
			}
		}else{
			switch(category){
			case "anime":
				convertView.setBackgroundResource(R.drawable.anime);
				break;
			case "information":
				convertView.setBackgroundResource(R.drawable.information);
				break;
			case "news":
				convertView.setBackgroundResource(R.drawable.news);
				break;
			case "sports":
				convertView.setBackgroundResource(R.drawable.sports);
				break;
			case "variety":
				convertView.setBackgroundResource(R.drawable.variety);
				break;
			case "drama":
				convertView.setBackgroundResource(R.drawable.drama);
				break;
			case "music":
				convertView.setBackgroundResource(R.drawable.music);
				break;
			case "cinema":
				convertView.setBackgroundResource(R.drawable.cinema);
				break;
			case "etc":
				convertView.setBackgroundResource(R.drawable.etc);
				break;
			}
		}

		holder.title.setText(item.getTitle());

		String start = new SimpleDateFormat("MM/dd (E) HH:mm", Locale.JAPANESE).format(new Date(item.getStart()));
		String end = new SimpleDateFormat("MM/dd (E) HH:mm", Locale.JAPANESE).format(new Date(item.getEnd()));

		String startDay = start.substring(0, 9);
		String endDay = end.substring(0, 9);

		if(startDay.equals(endDay))
			end = end.substring(10);

		holder.date.setText(start + " 〜 " + end);

		if(type == Type.RESERVES){
			Reserve reserve = (Reserve)getItem(position);
			if(!reserve.getIsManualReserved() && reserve.getIsSkip()){
				holder.title.setTextColor(Color.GRAY);
				holder.date.setTextColor(Color.GRAY);
				TextPaint titlePaint = holder.title.getPaint();
				TextPaint datePaint = holder.date.getPaint();
				titlePaint.setFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				titlePaint.setAntiAlias(true);
				datePaint.setFlags(holder.date.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				datePaint.setAntiAlias(true);
			}
		}
		return convertView;
	}
}