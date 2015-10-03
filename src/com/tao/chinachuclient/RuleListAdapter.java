package com.tao.chinachuclient;

import Chinachu4j.Rule;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RuleListAdapter extends ArrayAdapter<Rule>{
	private LayoutInflater mInflater;

	public RuleListAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	class ViewHolder{
		TextView title, channel;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		final Rule item = getItem(position);

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.program_list_layout, null);
			TextView title = (TextView)convertView.findViewById(R.id.program_title);
			TextView channel = (TextView)convertView.findViewById(R.id.program_date);

			holder = new ViewHolder();
			holder.title = title;
			holder.channel = channel;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		if(item.getCategories().length > 0) {
			switch(item.getCategories()[0]){
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
			default:
				break;
			}
		}

		String[] titles = item.getReserve_titles();
		String title = "";
		if(titles.length != 0) {
			for(String s : titles)
				title += s + ", ";
			title = title.substring(0, title.length() - 2);
		}else
			title = "any";

		String[] channels = item.getChannels();
		String channel = "";
		if(channels.length != 0) {
			for(String s : channels)
				channel += s + ", ";
			channel = channel.substring(0, channel.length() - 2);
		}else{
			channel = "any";
		}

		holder.title.setText(title);
		holder.channel.setText(channel);

		return convertView;
	}
}
