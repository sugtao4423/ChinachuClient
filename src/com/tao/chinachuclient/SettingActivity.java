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
		String user = Base64.encodeToString(username.getText().toString().getBytes(), Base64.DEFAULT);
		String passwd = Base64.encodeToString(password.getText().toString().getBytes(), Base64.DEFAULT);
		
		pref.edit()
		.putString("chinachuAddress", chinachuAddress.getText().toString())
		.putString("username", user)
		.putString("password", passwd)
		.commit();
		if(startMain){
			startActivity(new Intent(this, MainActivity.class));
		}else{
			Chinachu4j chinachu = new Chinachu4j(chinachuAddress.getText().toString(),
					username.getText().toString(), password.getText().toString());
			((ApplicationClass)getApplicationContext()).setChinachu(chinachu);
		}
		finish();
	}
}
