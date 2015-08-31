package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import Chinachu4j.Program;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

public class ProgramActivity extends Activity {
	
	private ListView list;
	private ProgramListAdapter programListAdapter;
	private ApplicationClass appClass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		list = new ListView(this);
		setContentView(list);
		
		programListAdapter = new ProgramListAdapter(this);
		list.setAdapter(programListAdapter);
		
		appClass = (ApplicationClass)getApplicationContext();
		//type 1: 予約済み 2: 録画中
		int type = getIntent().getIntExtra("type", -1);
		if(type == -1)
			finish();
		
		list.setOnItemClickListener(new ProgramListClickListener(this, type));

		switch(type){
		case 1:
			getActionBar().setTitle("予約済み");
			break;
		case 2:
			getActionBar().setTitle("録画中");
			break;
		case 3:
			getActionBar().setTitle("録画済み");
			break;
		}
		set(type);
	}
	
	public void set(final int type){
		AsyncTask<Void, Void, Program[]> task = new AsyncTask<Void, Void, Program[]>(){
			private ProgressDialog progDailog;
			@Override
	        protected void onPreExecute() {
	            progDailog = new ProgressDialog(ProgramActivity.this);
	            progDailog.setMessage("Loading...");
	            progDailog.setIndeterminate(false);
	            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progDailog.setCancelable(true);
	            progDailog.show();
	        }
			@Override
			protected Program[] doInBackground(Void... params) {
				try {
					if(type == 1)
						return appClass.getChinachu().getReserves();
					if(type == 2)
						return appClass.getChinachu().getRecording();
					if(type == 3)
						return appClass.getChinachu().getRecorded();
					
					return null;
				} catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(Program[] result){
				progDailog.dismiss();
				if(result == null){
					Toast.makeText(ProgramActivity.this,"番組取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				programListAdapter.addAll(result);
			}
		};
		task.execute();
	}
}
