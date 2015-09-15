package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import Chinachu4j.Chinachu4j;
import Chinachu4j.Program;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChannelScheduleActivity extends Activity implements OnNavigationListener{
	
	private ArrayAdapter<String> spinnerAdapter;
	
	private String[] channelIdList;
	
	private ListView programList;
	private ProgramListAdapter programListAdapter;
	
	private Chinachu4j chinachu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		programList = new ListView(this);
		setContentView(programList);
		
		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionbar.setTitle("番組表");
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(false);
		
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
		
		actionbar.setListNavigationCallbacks(spinnerAdapter, this);
		
		programListAdapter = new ProgramListAdapter(this);
		programList.setAdapter(programListAdapter);
		programList.setOnItemClickListener(new ProgramListClickListener(this, 0));
		
		chinachu = ((ApplicationClass)getApplicationContext()).getChinachu();
		
		//チャンネルリストの取得
		AsyncTask<Void, Void, String[]> task = new AsyncTask<Void, Void, String[]>(){
			private ProgressDialog progDailog;
			@Override
	        protected void onPreExecute() {
	            progDailog = new ProgressDialog(ChannelScheduleActivity.this);
	            progDailog.setMessage("Loading...");
	            progDailog.setIndeterminate(false);
	            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progDailog.setCancelable(true);
	            progDailog.show();
	        }
			@Override
			protected String[] doInBackground(Void... params) {
					try {
						return chinachu.getChannelList();
					} catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
						return new String[]{"[ERROR]", e.getMessage()};
					}
			}
			@Override
			protected void onPostExecute(String[] result){
				progDailog.dismiss();
				if(result[0].equals("[ERROR]")){
					Toast.makeText(ChannelScheduleActivity.this, result[1], Toast.LENGTH_LONG).show();
					return;
				}
				channelIdList = new String[result.length];
				for(int i = 0; i < result.length; i++)
					channelIdList[i] = result[i].split(",")[1];
				
				String[] channelNameList = new String[result.length];
				for(int i = 0; i < result.length; i++)
					channelNameList[i] = result[i].split(",")[0];
				spinnerAdapter.addAll(channelNameList);
			}
		};
		task.execute();
	}
	//ActionBarのSpinnerで選択された時呼ばれる
	@Override
	public boolean onNavigationItemSelected(final int itemPosition, long itemId) {
		AsyncTask<Void, Void, Program[]> task = new AsyncTask<Void, Void, Program[]>(){
			private ProgressDialog progDailog;
			@Override
	        protected void onPreExecute() {
	            progDailog = new ProgressDialog(ChannelScheduleActivity.this);
	            progDailog.setMessage("Loading...");
	            progDailog.setIndeterminate(false);
	            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progDailog.setCancelable(true);
	            progDailog.show();
	        }
			@Override
			protected Program[] doInBackground(Void... params) {
				try {
					return chinachu.getChannelSchedule(channelIdList[itemPosition]);
				} catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
					return null;
				}
			}
			@Override
			protected void onPostExecute(Program[] result){
				progDailog.dismiss();
				programListAdapter.clear();
				if(result == null){
					Toast.makeText(ChannelScheduleActivity.this,"番組取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				programListAdapter.addAll(result);
			}
		};
		task.execute();
		return false;
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home){
				finish();
				return true;
		}
	    return super.onOptionsItemSelected(item);
	}
}