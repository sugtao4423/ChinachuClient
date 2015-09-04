package com.tao.chinachuclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Chinachu4j.Program;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProgramListAdapter extends ArrayAdapter<Program>{
	private LayoutInflater mInflater;

	public ProgramListAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	class ViewHolder{
		TextView title, date;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		final Program item = getItem(position);

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

		if(position % 2 == 0)
			convertView.setBackgroundResource(R.drawable.position0);
		else
			convertView.setBackgroundResource(R.drawable.position1);

		holder.title.setText(item.getTitle());

		String start = new SimpleDateFormat("MM/dd HH:mm", Locale.JAPANESE).format(new Date(item.getStart()));
		String end = new SimpleDateFormat("MM/dd HH:mm", Locale.JAPANESE).format(new Date(item.getEnd()));

		holder.date.setText(start + " 〜 " + end);
		return convertView;
	}
}