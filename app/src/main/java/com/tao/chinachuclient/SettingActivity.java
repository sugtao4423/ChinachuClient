package com.tao.chinachuclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tao.chinachuclient.data.Encode;
import com.tao.chinachuclient.data.Server;

public class SettingActivity extends AppCompatActivity{

    private EditText chinachuAddress, username, password;
    private SharedPreferences pref;

    private Spinner type, videoBitrateFormat, audioBitrateFormat;
    private EditText containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame;
    private SharedPreferences enc;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        enc = getSharedPreferences("encodeConfig", MODE_PRIVATE);

        new AlertDialog.Builder(this)
                .setTitle(R.string.change_settings)
                .setMessage(getString(R.string.change_current_server_settings) + "\n\n" + getString(R.string.current_server) + "\n" + pref.getString("chinachuAddress", ""))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        finish();
                    }
                }).show();

        chinachuAddress = (EditText)findViewById(R.id.chinachuAddress);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        chinachuAddress.setText(pref.getString("chinachuAddress", ""));
        username.setText(new String(Base64.decode(pref.getString("username", ""), Base64.DEFAULT)));
        password.setText(new String(Base64.decode(pref.getString("password", ""), Base64.DEFAULT)));

        type = (Spinner)findViewById(R.id.enc_setting_type_spinner);
        containerFormat = (EditText)findViewById(R.id.enc_setting_container_edit);
        videoCodec = (EditText)findViewById(R.id.enc_setting_videoCodec_edit);
        audioCodec = (EditText)findViewById(R.id.enc_setting_audioCodec_edit);

        videoBitrate = (EditText)findViewById(R.id.enc_setting_videoBitrate);
        videoBitrateFormat = (Spinner)findViewById(R.id.enc_setting_video_bitrate_spinner);
        audioBitrate = (EditText)findViewById(R.id.enc_setting_audioBitrate);
        audioBitrateFormat = (Spinner)findViewById(R.id.enc_setting_audio_bitrate_spinner);
        videoSize = (EditText)findViewById(R.id.enc_setting_videoSize);
        frame = (EditText)findViewById(R.id.enc_setting_frame);

        switch(enc.getString("type", "")){
            case "mp4":
                type.setSelection(0);
                break;
            case "m2ts":
                type.setSelection(1);
                break;
            case "webm":
                type.setSelection(2);
                break;
        }
        containerFormat.setText(enc.getString("containerFormat", ""));
        videoCodec.setText(enc.getString("videoCodec", ""));
        audioCodec.setText(enc.getString("audioCodec", ""));

        String prefVideoBitrate = enc.getString("videoBitrate", "0");
        int videoBit = prefVideoBitrate.isEmpty() ? 0 : Integer.parseInt(prefVideoBitrate);
        if((videoBit / 1000 / 1000) != 0){
            videoBitrateFormat.setSelection(1);
            videoBitrate.setText(String.valueOf(videoBit / 1000 / 1000));
        }else if((videoBit / 1000) != 0){
            videoBitrateFormat.setSelection(0);
            videoBitrate.setText(String.valueOf(videoBit / 1000));
        }else{
            videoBitrate.setText("");
        }

        String prefAudioBitrate = enc.getString("audioBitrate", "0");
        int audioBit = prefAudioBitrate.isEmpty() ? 0 : Integer.parseInt(prefAudioBitrate);
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
        String raw_chinachuAddress = chinachuAddress.getText().toString();
        if(!(raw_chinachuAddress.startsWith("http://") || raw_chinachuAddress.startsWith("https://"))){
            Toast.makeText(this, R.string.wrong_server_address, Toast.LENGTH_SHORT).show();
            return;
        }

        String oldChinachuAddress = pref.getString("chinachuAddress", "");

        Encode encode = ((ApplicationClass)getApplicationContext()).getEncodeSetting(
                type, containerFormat, videoCodec, audioCodec,
                videoBitrate, videoBitrateFormat, audioBitrate, audioBitrateFormat, videoSize, frame);

        Server server = new Server(raw_chinachuAddress,
                Base64.encodeToString(username.getText().toString().getBytes(), Base64.DEFAULT),
                Base64.encodeToString(password.getText().toString().getBytes(), Base64.DEFAULT),
                pref.getBoolean("streaming", false),
                pref.getBoolean("encStreaming", false),
                encode,
                null, null,
                pref.getBoolean("oldCategoryColor", false));

        pref.edit()
                .putString("chinachuAddress", server.getChinachuAddress())
                .putString("username", server.getUsername())
                .putString("password", server.getPassword())
                .commit();
        enc.edit()
                .putString("type", encode.getType())
                .putString("containerFormat", encode.getContainerFormat())
                .putString("videoCodec", encode.getVideoCodec())
                .putString("audioCodec", encode.getAudioCodec())
                .putString("videoBitrate", encode.getVideoBitrate())
                .putString("audioBitrate", encode.getAudioBitrate())
                .putString("videoSize", encode.getVideoSize())
                .putString("frame", encode.getFrame())
                .commit();

        DBUtils dbUtils = new DBUtils(this);
        dbUtils.updateServer(server, oldChinachuAddress, this);
        dbUtils.close();
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