package com.tao.chinachuclient;

import Chinachu4j.Chinachu4j;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener{
	
	private SharedPreferences pref;
	private Chinachu4j chinachu;
	private ApplicationClass appClass;
	private String chinachuAddress, username, password;
	private ListView mainList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		appClass = (ApplicationClass)getApplicationContext();
		
		chinachuAddress = pref.getString("chinachuAddress", null);
		username = pref.getString("username", null);
		password = pref.getString("password", null);
		if(chinachuAddress == null || username == null || password == null){
			Intent i = new Intent(this, SettingActivity.class);
			i.putExtra("startMain", true);
			startActivity(i);
			finish();
			return;
		}
		username = new String(Base64.decode(username,Base64.DEFAULT));
		password = new String(Base64.decode(password,Base64.DEFAULT));
		
		mainList = (ListView)findViewById(R.id.mainList);
		String[] listItem = new String[]{"番組表", "ルール", "予約済み", "録画中", "録画済み"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem);
		mainList.setAdapter(adapter);
		mainList.setOnItemClickListener(this);
		
		chinachu = new Chinachu4j(chinachuAddress, username, password);
		
		appClass.setChinachu(chinachu);
		
		appClass.setStreaming(pref.getBoolean("streaming", false));
		appClass.setEncStreaming(pref.getBoolean("encStreaming", false));
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(position == 0){
			startActivity(new Intent(this, ChannelScheduleActivity.class));
		}
		if(position > 1){
			Intent i = new Intent(this, ProgramActivity.class);
			i.putExtra("type", position);
			startActivity(i);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add("設定");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getTitle().equals("設定"))
			startActivity(new Intent(this, Preference.class));
		return super.onOptionsItemSelected(item);
	}
}