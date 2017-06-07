package com.tao.chinachuclient;

import Chinachu4j.Program;
import Chinachu4j.Recorded;
import Chinachu4j.Reserve;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ProgramListClickListener implements OnItemClickListener{

	private Context context;
	private int type;

	public ProgramListClickListener(Context context, int type){
		this.context = context;
		this.type = type;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		Intent i = new Intent(context, ProgramDetail.class);
		if(type == Type.RESERVES)
			i.putExtra("reserve", (Reserve)parent.getItemAtPosition(position));
		else if(type == Type.RECORDED)
			i.putExtra("recorded", (Recorded)parent.getItemAtPosition(position));
		else
			i.putExtra("program", (Program)parent.getItemAtPosition(position));
		i.putExtra("type", type);
		context.startActivity(i);
	}
}