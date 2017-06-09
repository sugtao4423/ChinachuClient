package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.json.JSONException;

import com.tao.chinachuclient.data.Encode;
import com.tao.chinachuclient.data.Server;

import Chinachu4j.Chinachu4j;
import Chinachu4j.Program;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddServer extends Activity{

	private EditText chinachuAddress, username, password;
	private boolean startMain;

	private Spinner type, containerFormat, videoCodec, audioCodec, videoBitrateFormat, audioBitrateFormat;
	private EditText videoBitrate, audioBitrate, videoSize, frame;

	private String raw_chinachuAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("サーバー追加");

		startMain = getIntent().getBooleanExtra("startMain", false);

		chinachuAddress = (EditText)findViewById(R.id.chinachuAddress);
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);

		type = (Spinner)findViewById(R.id.enc_setting_type_spinner);
		containerFormat = (Spinner)findViewById(R.id.enc_setting_container_spinner);
		videoCodec = (Spinner)findViewById(R.id.enc_setting_videoCodec_spinner);
		audioCodec = (Spinner)findViewById(R.id.enc_setting_audioCodec_spinner);

		videoBitrate = (EditText)findViewById(R.id.enc_setting_videoBitrate);
		videoBitrateFormat = (Spinner)findViewById(R.id.enc_setting_video_bitrate_spinner);
		audioBitrate = (EditText)findViewById(R.id.enc_setting_audioBitrate);
		audioBitrateFormat = (Spinner)findViewById(R.id.enc_setting_audio_bitrate_spinner);
		videoSize = (EditText)findViewById(R.id.enc_setting_videoSize);
		frame = (EditText)findViewById(R.id.enc_setting_frame);
	}

	public void ok(View v){
		raw_chinachuAddress = chinachuAddress.getText().toString();
		if(!(raw_chinachuAddress.startsWith("http://") || raw_chinachuAddress.startsWith("https://"))) {
			Toast.makeText(this, "サーバーアドレスが間違っています", Toast.LENGTH_SHORT).show();
			return;
		}

		final DBUtils dbUtils = new DBUtils(this);
		if(dbUtils.serverExists(raw_chinachuAddress)){
			Toast.makeText(this, "すでに登録されています", Toast.LENGTH_SHORT).show();
			dbUtils.close();
			return;
		}

		new AsyncTask<Void, Void, Program[]>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(AddServer.this);
				progDialog.setMessage("チャンネルリストの取得中...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);
				progDialog.show();
			}

			@Override
			protected Program[] doInBackground(Void... params){
				try{
					Chinachu4j chinachu = new Chinachu4j(raw_chinachuAddress, username.getText().toString(), password.getText().toString());
					return chinachu.getAllSchedule();
				}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Program[] result){
				progDialog.dismiss();
				if(result == null){
					Toast.makeText(AddServer.this, "チャンネル取得に失敗しました\nやり直してください", Toast.LENGTH_SHORT).show();
					return;
				}
				ArrayList<String> id_name = new ArrayList<String>();
				for(Program p : result){
					if(id_name.indexOf(p.getChannel().getId() + "," + p.getChannel().getName()) == -1){
						id_name.add(p.getChannel().getId() + "," + p.getChannel().getName());
					}
				}
				String channelIds = "";
				String channelNames = "";
				for(String s : id_name){
					channelIds += s.split(",")[0] + ",";
					channelNames += s.split(",")[1] + ",";
				}

				String vb = null;
				String ab = null;
				if(!videoBitrate.getText().toString().isEmpty()) {
					int videoBit = Integer.parseInt(videoBitrate.getText().toString());
					if(videoBitrateFormat.getSelectedItemPosition() == 0)
						videoBit *= 1000;
					else
						videoBit *= 1000000;
					vb = String.valueOf(videoBit);
				}

				if(!audioBitrate.getText().toString().isEmpty()) {
					int audioBit = Integer.parseInt(audioBitrate.getText().toString());
					if(audioBitrateFormat.getSelectedItemPosition() == 0)
						audioBit *= 1000;
					else
						audioBit *= 1000000;
					ab = String.valueOf(audioBit);
				}

				Encode encode = new Encode((String)type.getSelectedItem(),
						(String)containerFormat.getSelectedItem(),
						(String)videoCodec.getSelectedItem(),
						(String)audioCodec.getSelectedItem(),
						vb,
						ab,
						videoSize.getText().toString().isEmpty() ? null : videoSize.getText().toString(),
						frame.getText().toString().isEmpty() ? null : frame.getText().toString());

				Server server = new Server(raw_chinachuAddress,
						Base64.encodeToString(username.getText().toString().getBytes(), Base64.DEFAULT),
						Base64.encodeToString(password.getText().toString().getBytes(), Base64.DEFAULT),
						false, false, encode, channelIds, channelNames, false);

				dbUtils.insertServer(server);
				dbUtils.close();

				if(startMain) {
					dbUtils.serverPutPref(AddServer.this, server);
					startActivity(new Intent(AddServer.this, MainActivity.class));
				}
				finish();
			}
		}.execute();
	}

	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
