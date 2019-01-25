package com.tao.chinachuclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Chinachu4j.Chinachu4j;
import Chinachu4j.ChinachuResponse;
import Chinachu4j.Program;
import Chinachu4j.Recorded;
import Chinachu4j.Reserve;

public class ProgramDetail extends AppCompatActivity{

    private Program program;
    private int type;
    private ApplicationClass appClass;
    private String capture;
    private int randomSecond;
    private boolean reserveIsManualReserved;
    private boolean reserveIsSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_detail);

        appClass = (ApplicationClass)getApplicationContext();
        type = getIntent().getIntExtra("type", -1);

        if(type == Type.RESERVES){
            Reserve reserve = (Reserve)getIntent().getSerializableExtra("reserve");
            program = reserve.getProgram();
            reserveIsManualReserved = reserve.getIsManualReserved();
            reserveIsSkip = reserve.getIsSkip();
        }else if(type == Type.RECORDED){
            Recorded recorded = (Recorded)getIntent().getSerializableExtra("recorded");
            program = recorded.getProgram();
        }else{
            program = (Program)getIntent().getSerializableExtra("program");
        }

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(program.getTitle());

        String detail = program.getDetail();
        long start = program.getStart();
        long end = program.getEnd();
        String category = program.getCategory();
        String[] flags = program.getFlags();
        String channelType = program.getChannel().getType();
        String channelName = program.getChannel().getName();

        final ImageView image = (ImageView)findViewById(R.id.programs_detail_image);
        if(type == Type.RECORDING || type == Type.RECORDED)
            image.setVisibility(View.VISIBLE);
        else
            image.setVisibility(View.GONE);

        detail = detail.replace("\n", "<br />");
        Matcher m = Pattern.compile("http(s)?://[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+").matcher(detail);
        while(m.find())
            detail = detail.replace(m.group(), String.format("<a href=\"%s\">%s</a>", m.group(), m.group()));
        String detailText = "<p><strong>フルタイトル</strong><br />" + program.getFullTitle() + "<br /></p><p><strong>詳細</strong><br />" + detail + "</p>";

        TextView detailView = (TextView)findViewById(R.id.program_detail_detail);
        detailView.setText(Html.fromHtml(detailText));
        detailView.setMovementMethod(SelectionLinkMovementMethod.getInstance(this));

        TextView otherView = (TextView)findViewById(R.id.program_detail_other);

        String startStr = new SimpleDateFormat("yyyy/MM/dd (E) HH:mm", Locale.JAPANESE).format(new Date(start));
        String endStr = new SimpleDateFormat("HH:mm", Locale.JAPANESE).format(new Date(end));
        String minute = program.getSeconds() / 60 + "分間";
        String flag = "";
        for(String s : flags)
            flag += s + ", ";
        if(flag.length() == 0)
            flag = "なし";
        else
            flag = flag.substring(0, flag.length() - 2);
        String otherText = "<p>" + startStr + " 〜 " + endStr + " (" + minute + ")<br /><br />" + category + " / " + channelType
                + ": " + channelName + "<br /><br />フラグ: " + flag + "<br /><br />id: " + program.getId() + "</p>";
        otherView.setText(Html.fromHtml(otherText));

        if(type == Type.RECORDING || type == Type.RECORDED){
            new AsyncTask<Void, Void, String>(){
                @Override
                protected String doInBackground(Void... params){
                    try{
                        if(type == Type.RECORDING)
                            return appClass.getChinachu().getRecordingImage(program.getId(), "1280x720");
                        if(type == Type.RECORDED){
                            randomSecond = new Random().nextInt(program.getSeconds()) + 1;
                            return appClass.getChinachu().getRecordedImage(program.getId(), randomSecond, "1280x720");
                        }
                        return null;
                    }catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(String result){
                    if(result == null){
                        Toast.makeText(ProgramDetail.this, R.string.error_get_image, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(result.startsWith("data:image/jpeg;base64,"))
                        result = result.substring(23);
                    byte[] decodedString = Base64.decode(result, Base64.DEFAULT);
                    Bitmap img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image.setImageBitmap(img);
                    capture = result;
                }
            }.execute();
        }
    }

    @SuppressLint("InflateParams")
    public void imageClick(View v){
        View view = LayoutInflater.from(this).inflate(R.layout.capture_dialog, null);
        final EditText cap_pos = (EditText)view.findViewById(R.id.cap_pos);
        final EditText cap_size = (EditText)view.findViewById(R.id.cap_size);
        final SeekBar cap_seek = (SeekBar)view.findViewById(R.id.cap_seek);

        if(type == Type.RECORDING){
            cap_pos.setVisibility(View.GONE);
            cap_seek.setVisibility(View.GONE);
        }else if(type == Type.RECORDED){
            cap_pos.setText(String.valueOf(randomSecond));
            final float textSize = cap_pos.getTextSize();
            cap_pos.setWidth((int)((String.valueOf(program.getSeconds()).length() + 1) * textSize));
            cap_seek.setMax(program.getSeconds() - 10);
            cap_seek.setProgress(randomSecond);
            cap_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

                @Override
                public void onStopTrackingTouch(SeekBar seekBar){
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar){
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                    cap_pos.setText(String.valueOf(progress));
                }
            });
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        new AsyncTask<Void, Void, String>(){
                            private ProgressDialog progDialog;

                            @Override
                            protected void onPreExecute(){
                                progDialog = new ProgressDialog(ProgramDetail.this);
                                progDialog.setMessage(getString(R.string.loading));
                                progDialog.setIndeterminate(false);
                                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progDialog.setCancelable(true);
                                progDialog.show();
                            }

                            @Override
                            protected String doInBackground(Void... params){
                                try{
                                    if(type == Type.RECORDING){
                                        return appClass.getChinachu().getRecordingImage(program.getId(), cap_size.getText().toString());
                                    }
                                    if(type == Type.RECORDED){
                                        return appClass.getChinachu().getRecordedImage(program.getId(), Integer.parseInt(cap_pos.getText().toString()),
                                                cap_size.getText().toString());
                                    }
                                    return null;
                                }catch(KeyManagementException | NumberFormatException | NoSuchAlgorithmException | IOException e){
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(String result){
                                progDialog.dismiss();
                                if(result == null){
                                    Toast.makeText(ProgramDetail.this, R.string.error_get_image, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(result.startsWith("data:image/jpeg;base64,"))
                                    result = result.substring(23);
                                Intent i = new Intent(ProgramDetail.this, ShowImage.class);
                                i.putExtra("base64", result);
                                i.putExtra("programId", program.getId());
                                if(type == Type.RECORDED)
                                    i.putExtra("pos", Integer.parseInt(cap_pos.getText().toString()));
                                startActivity(i);
                            }
                        }.execute();
                    }
                }).setNeutralButton(R.string.zoom_this_state, new OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                Intent i = new Intent(ProgramDetail.this, ShowImage.class);
                i.putExtra("base64", capture);
                i.putExtra("programId", program.getId());
                if(type == Type.RECORDED)
                    i.putExtra("pos", Integer.parseInt(cap_pos.getText().toString()));
                startActivity(i);
            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(type == Type.CHANNEL_SCHEDULE_ACTIVITY || type == Type.SEARCH_PROGRAM){
            menu.add(0, Menu.FIRST, Menu.NONE, R.string.reserve);
        }else if(type == Type.RESERVES){
            if(reserveIsManualReserved){
                menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.delete_reserve);
            }else{
                if(reserveIsSkip){
                    menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.skip_reserve_release);
                }else{
                    menu.add(0, Menu.FIRST + 1, Menu.NONE, R.string.skip_reserve);
                }
            }
        }else if(type == Type.RECORDING || type == Type.RECORDED){
            if(appClass.getStreaming()){
                menu.add(0, Menu.FIRST + 2, Menu.NONE, R.string.streaming_play);
            }
            if(appClass.getEncStreaming()){
                menu.add(0, Menu.FIRST + 3, Menu.NONE, R.string.streaming_play_encode);
            }
        }
        if(type == Type.RECORDED){
            menu.add(0, Menu.FIRST + 4, Menu.NONE, R.string.delete_recorded_file);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
        }else if(item.getItemId() == Menu.FIRST + 2){
            if(type == Type.RECORDING){
                Uri uri = Uri.parse(appClass.getChinachu().getNonEncRecordingMovieURL(program.getId()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            if(type == Type.RECORDED){
                Uri uri = Uri.parse(appClass.getChinachu().getNonEncRecordedMovieURL(program.getId()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }else if(item.getItemId() == Menu.FIRST + 3){
            String[] params = new String[7];
            SharedPreferences enc = getSharedPreferences("encodeConfig", MODE_PRIVATE);
            String t = enc.getString("type", null);
            params[0] = enc.getString("containerFormat", null);
            params[1] = enc.getString("videoCodec", null);
            params[2] = enc.getString("audioCodec", null);
            params[3] = enc.getString("videoBitrate", null);
            params[4] = enc.getString("audioBitrate", null);
            params[5] = enc.getString("videoSize", null);
            params[6] = enc.getString("frame", null);
            if(type == Type.RECORDING){
                Uri uri = Uri.parse(appClass.getChinachu().getEncRecordingMovieURL(program.getId(), t, params));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            if(type == Type.RECORDED){
                Uri uri = Uri.parse(appClass.getChinachu().getEncRecordedMovieURL(program.getId(), t, params));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }else if(type == Type.CHANNEL_SCHEDULE_ACTIVITY || type == Type.RESERVES || type == Type.RECORDED || type == Type.SEARCH_PROGRAM){
            confirm();
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirm(){
        final Chinachu4j chinachu = appClass.getChinachu();

        AlertDialog.Builder before = new Builder(this);
        switch(type){
            case Type.CHANNEL_SCHEDULE_ACTIVITY:
            case Type.SEARCH_PROGRAM:
                before.setTitle(R.string.is_reserve);
                break;
            case Type.RESERVES:
                if(reserveIsManualReserved){
                    before.setTitle(R.string.is_delete_reserve);
                }else{
                    if(reserveIsSkip){
                        before.setTitle(R.string.is_skip_reserve_release);
                    }else{
                        before.setTitle(R.string.is_skip_reserve);
                    }
                }
                break;
            case Type.RECORDED:
                before.setTitle(R.string.is_delete_recorded_file);
                break;
        }
        before.setMessage(program.getFullTitle()).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.ok, new OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                new AsyncTask<Void, Void, ChinachuResponse>(){
                    private ProgressDialog progDialog;

                    @Override
                    protected void onPreExecute(){
                        progDialog = new ProgressDialog(ProgramDetail.this);
                        progDialog.setMessage(getString(R.string.sending));
                        progDialog.setIndeterminate(false);
                        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progDialog.setCancelable(true);
                        progDialog.show();
                    }

                    @Override
                    protected ChinachuResponse doInBackground(Void... params){
                        try{
                            switch(type){
                                case Type.CHANNEL_SCHEDULE_ACTIVITY:
                                case Type.SEARCH_PROGRAM:
                                    return chinachu.putReserve(program.getId());
                                case Type.RESERVES:
                                    if(reserveIsManualReserved){
                                        return chinachu.delReserve(program.getId());
                                    }else{
                                        if(reserveIsSkip)
                                            return chinachu.reserveUnskip(program.getId());
                                        else
                                            return chinachu.reserveSkip(program.getId());
                                    }
                                case Type.RECORDED:
                                    return chinachu.delRecordedFile(program.getId());
                            }
                            return null;
                        }catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(ChinachuResponse result){
                        progDialog.dismiss();
                        if(result == null){
                            Toast.makeText(ProgramDetail.this, R.string.error_access, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(!result.getResult()){
                            Toast.makeText(ProgramDetail.this, result.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        AlertDialog.Builder after = new Builder(ProgramDetail.this);
                        switch(type){
                            case Type.CHANNEL_SCHEDULE_ACTIVITY:
                            case Type.SEARCH_PROGRAM:
                                after.setTitle(R.string.done_reserve);
                                after.setMessage(program.getFullTitle());
                                break;
                            case Type.RESERVES:
                                if(reserveIsManualReserved){
                                    after.setTitle(R.string.done_delete_reverse);
                                }else{
                                    if(reserveIsSkip){
                                        after.setTitle(R.string.done_skip_reserve_release);
                                    }else{
                                        after.setTitle(R.string.done_skip_reserve);
                                    }
                                }
                                after.setMessage(program.getFullTitle());
                                appClass.setReloadList(true);
                                break;
                            case Type.RECORDED:
                                after.setTitle(R.string.done_delete_recorded_file);
                                after.setMessage(program.getFullTitle() + "\n\n" + getString(R.string.reflect_recorded_list_need_cleanup));
                                break;
                        }
                        after.show();
                    }
                }.execute();
            }
        }).show();
    }
}