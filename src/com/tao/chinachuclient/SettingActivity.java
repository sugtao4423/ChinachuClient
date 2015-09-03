package com.tao.chinachuclient;

import Chinachu4j.Chinachu4j;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends Activity {
	
	private EditText chinachuAddress, username, password;
	private SharedPreferences pref;
	private boolean startMain;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		startMain = getIntent().getBooleanExtra("startMain", false);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		chinachuAddress = (EditText)findViewById(R.id.chinachuAddress);
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
		
		chinachuAddress.setText(pref.getString("chinachuAddress", ""));
		username.setText(new String(Base64.decode(pref.getString("username", ""),Base64.DEFAULT)));
		password.setText(new String(Base64.decode(pref.getString("password", ""),Base64.DEFAULT)));
	}
	
	public void ok(View v){
		String raw_chinachuAddress = chinachuAddress.getText().toString();
		if(!(raw_chinachuAddress.startsWith("http://") || raw_chinachuAddress.startsWith("https://"))){
			Toast.makeText(this, "サーバーアドレスが間違っています", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String raw_username = username.getText().toString();
		String raw_password = password.getText().toString();
		
		String user = Base64.encodeToString(raw_username.getBytes(), Base64.DEFAULT);
		String passwd = Base64.encodeToString(raw_password.getBytes(), Base64.DEFAULT);
		
		pref.edit()
		.putString("chinachuAddress", raw_chinachuAddress)
		.putString("username", user)
		.putString("password", passwd)
		.commit();
		if(startMain){
			startActivity(new Intent(this, MainActivity.class));
		}else{
			Chinachu4j chinachu = new Chinachu4j(raw_chinachuAddress, raw_username, raw_password);
			((ApplicationClass)getApplicationContext()).setChinachu(chinachu);
		}
		finish();
	}
}
