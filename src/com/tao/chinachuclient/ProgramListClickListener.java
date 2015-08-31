package com.tao.chinachuclient;

import Chinachu4j.Program;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ProgramListClickListener implements OnItemClickListener {
	
	private Context context;
	private int type;

	public ProgramListClickListener(Context context, int type) {
		this.context = context;
		this.type = type;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Program item = (Program)parent.getItemAtPosition(position);
		Intent i = new Intent(context, ProgramDetail.class);
		i.putExtra("id", item.getId());
		i.putExtra("title", item.getTitle());
		i.putExtra("fullTitle", item.getFullTitle());
		i.putExtra("detail", item.getDetail());
		i.putExtra("start", item.getStart());
		i.putExtra("end", item.getEnd());
		i.putExtra("seconds", item.getSeconds());
		i.putExtra("category", item.getCategory());
		i.putExtra("flags", item.getFlags());
		i.putExtra("channelType", item.getChannel().getType());
		i.putExtra("channelName", item.getChannel().getName());
		i.putExtra("type", type);
		context.startActivity(i);
	}
}