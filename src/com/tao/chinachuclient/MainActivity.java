package com.tao.chinachuclient;

import java.util.ArrayList;

import Chinachu4j.Chinachu4j;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
		mainList = new ListView(this);
		setContentView(mainList);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		appClass = (ApplicationClass)getApplicationContext();
		
		chinachuAddress = pref.getString("chinachuAddress", null);
		username = pref.getString("username", null);
		password = pref.getString("password", null);
		if(chinachuAddress == null || username == null || password == null){
			Intent i = new Intent(this, AddServer.class);
			i.putExtra("startMain", true);
			startActivity(i);
			finish();
			return;
		}
		username = new String(Base64.decode(username,Base64.DEFAULT));
		password = new String(Base64.decode(password,Base64.DEFAULT));
		
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
		if(position == 0)
			startActivity(new Intent(this, ChannelScheduleActivity.class));
		if(position == 1)
			startActivity(new Intent(this, RuleActivity.class));
		if(position > 1){
			Intent i = new Intent(this, ProgramActivity.class);
			i.putExtra("type", position);
			startActivity(i);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem changeServer = menu.add("鯖変更");
		changeServer.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		MenuItem item = menu.add("設定");
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getTitle().equals("設定"))
			startActivity(new Intent(this, Preference.class));
		if(item.getTitle().equals("鯖変更")){
			final SQLiteDatabase db = new ServerSQLHelper(this).getReadableDatabase();
			final ArrayList<String> address = new ArrayList<String>();
			Cursor result = db.rawQuery("select chinachuAddress from servers", null);
			boolean mov = result.moveToFirst();
			while(mov){
				address.add(result.getString(0));
				mov = result.moveToNext();
			}
			int settingNow = address.indexOf(pref.getString("chinachuAddress", ""));
			AlertDialog.Builder selectServer = new AlertDialog.Builder(this);
			selectServer.setTitle("サーバーを選択してください")
			.setSingleChoiceItems((String[])address.toArray(new String[0]), settingNow, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					String selectedAddress = address.get(which);
					Cursor allGet = db.rawQuery("select * from servers where chinachuAddress=?", new String[]{selectedAddress});
					allGet.moveToFirst();
					String ca = allGet.getString(0);
					String u = allGet.getString(1);
					String p = allGet.getString(2);
					boolean s = Boolean.valueOf(allGet.getString(3));
					boolean encS = Boolean.valueOf(allGet.getString(4));
					pref.edit()
					.putString("chinachuAddress", ca)
					.putString("username", u)
					.putString("password", p)
					.putBoolean("streaming", s)
					.putBoolean("encStreaming", encS)
					.commit();
					chinachu = new Chinachu4j(ca, new String(Base64.decode(u,Base64.DEFAULT)), new String(Base64.decode(p,Base64.DEFAULT)));
					appClass.setChinachu(chinachu);
					appClass.setStreaming(s);
					appClass.setStreaming(encS);
					
					SharedPreferences enc = getSharedPreferences("encodeConfig", MODE_PRIVATE);
					String[] encode = new String[8];
					encode[0] = allGet.getString(5);
					encode[1] = allGet.getString(6);
					encode[2] = allGet.getString(7);
					encode[3] = allGet.getString(8);
					encode[4] = allGet.getString(9);
					encode[5] = allGet.getString(10);
					encode[6] = allGet.getString(11);
					encode[7] = allGet.getString(12);
					for(int i = 0; i < encode.length; i++){
						if(encode[i].isEmpty() || encode[i].equals("null"))
							encode[i] = null;
					}
					enc.edit()
					.putString("type", encode[0])
					.putString("containerFormat", encode[1])
					.putString("videoCodec", encode[2])
					.putString("audioCodec", encode[3])
					.putString("videoBitrate", encode[4])
					.putString("audioBitrate", encode[5])
					.putString("videoSize", encode[6])
					.putString("frame", encode[7])
					.commit();
				}
			})
			.setPositiveButton("OK", null);
			selectServer.create().show();
		}
		return super.onOptionsItemSelected(item);
	}
}