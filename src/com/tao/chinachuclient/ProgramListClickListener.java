package com.tao.chinachuclient;

import Chinachu4j.Program;
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
		Program item = (Program)parent.getItemAtPosition(position);
		((ApplicationClass)context.getApplicationContext()).setTmp(item);
		Intent i = new Intent(context, ProgramDetail.class);
		i.putExtra("type", type);
		context.startActivity(i);
	}
}