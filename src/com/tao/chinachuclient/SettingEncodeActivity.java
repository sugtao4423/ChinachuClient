package com.tao.chinachuclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingEncodeActivity extends Activity{

	private SharedPreferences enc;

	private Spinner type, containerFormat, videoCodec, audioCodec, videoBitrateFormat, audioBitrateFormat;
	private EditText videoBitrate, audioBitrate, videoSize, frame;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enc_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		enc = getSharedPreferences("encodeConfig", MODE_PRIVATE);

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
		
		switch(enc.getString("type", "")){
		case "m2ts":
			type.setSelection(0);
			break;
		case "f4v":
			type.setSelection(1);
			break;
		case "flv":
			type.setSelection(2);
			break;
		case "webm":
			type.setSelection(3);
			break;
		case "asf":
			type.setSelection(4);
			break;
		}
		switch(enc.getString("containerFormat", "")){
		case "mpegts":
			containerFormat.setSelection(0);
			break;
		case "flv":
			containerFormat.setSelection(1);
			break;
		case "asf":
			containerFormat.setSelection(2);
			break;
		case "webm":
			containerFormat.setSelection(3);
			break;
		}
		switch(enc.getString("videoCodec", "")){
		case "copy":
			videoCodec.setSelection(0);
			break;
		case "libvpx":
			videoCodec.setSelection(1);
			break;
		case "flv":
			videoCodec.setSelection(2);
			break;
		case "libx264":
			videoCodec.setSelection(3);
			break;
		case "wmv2":
			videoCodec.setSelection(4);
			break;
		}
		switch(enc.getString("audioCodec", "")){
		case "copy":
			audioCodec.setSelection(0);
			break;
		case "libvorbis":
			audioCodec.setSelection(1);
			break;
		case "libfdk_aac":
			audioCodec.setSelection(2);
			break;
		case "wmav2":
			audioCodec.setSelection(3);
			break;
		}

		int videoBit = Integer.parseInt(enc.getString("videoBitrate", "0"));
		if((videoBit / 1000 / 1000) != 0){
			videoBitrateFormat.setSelection(1);
			videoBitrate.setText(String.valueOf(videoBit / 1000 / 1000));
		}else if((videoBit / 1000) != 0){
			videoBitrateFormat.setSelection(0);
			videoBitrate.setText(String.valueOf(videoBit / 1000));
		}else{
			videoBitrate.setText("");
		}
		
		int audioBit = Integer.parseInt(enc.getString("audioBitrate", "0"));
		if((audioBit / 1000 / 1000) != 0){
			audioBitrateFormat.setSelection(1);
			audioBitrate.setText(String.valueOf(audioBit / 1000 / 1000));
		}else if((audioBit / 1000) != 0){
			audioBitrateFormat.setSelection(0);
			audioBitrate.setText(String.valueOf(audioBit / 1000));
		}else{
			audioBitrate.setText("");
		}
		
		videoSize.setText(enc.getString("videoSize", ""));
		frame.setText(enc.getString("frame", ""));
	}

	public void ok(View v){
		String vb = null;
		String ab = null;
		if(!videoBitrate.getText().toString().isEmpty()){
			int videoBit = Integer.parseInt(videoBitrate.getText().toString());
			if(videoBitrateFormat.getSelectedItemPosition() == 0)
				videoBit *= 1000;
			else
				videoBit *= 1000000;
			vb = String.valueOf(videoBit);
		}
		
		if(!audioBitrate.getText().toString().isEmpty()){
			int audioBit = Integer.parseInt(audioBitrate.getText().toString());
			if(audioBitrateFormat.getSelectedItemPosition() == 0)
				audioBit *= 1000;
			else
				audioBit *= 1000000;
			ab = String.valueOf(audioBit);
		}
		
		enc.edit().putString("type", (String)type.getSelectedItem())
				.putString("containerFormat", (String)containerFormat.getSelectedItem())
				.putString("videoCodec", (String)videoCodec.getSelectedItem())
				.putString("audioCodec", (String)audioCodec.getSelectedItem())
				.putString("videoBitrate", vb)
				.putString("audioBitrate", ab)
				.putString("videoSize", videoSize.getText().toString()).putString("frame", frame.getText().toString())
				.commit();
		finish();
	}

	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
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