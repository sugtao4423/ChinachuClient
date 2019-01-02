package com.tao.chinachuclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import Chinachu4j.Chinachu4j;
import Chinachu4j.Program;

public class ChannelScheduleActivity extends AppCompatActivity implements ActionBar.OnNavigationListener{

    private ArrayAdapter<String> spinnerAdapter;

    private String[] channelIdList;
    private String showingChannelId;

    private ListView programList;
    private ProgramListAdapter programListAdapter;

    private Chinachu4j chinachu;
    private ApplicationClass appClass;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        programList = new ListView(this);
        setContentView(programList);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionbar.setTitle("番組表");
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(false);

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        // チャンネルリストの取得
        SharedPreferences channels = getSharedPreferences("channels", MODE_PRIVATE);
        channelIdList = channels.getString("channelIds", null).split(",", 0);
        spinnerAdapter.addAll(channels.getString("channelNames", null).split(",", 0));

        programListAdapter = new ProgramListAdapter(this, 0);
        programList.setAdapter(programListAdapter);
        programList.setOnItemClickListener(new ProgramListClickListener(this, 0));

        appClass = (ApplicationClass)getApplicationContext();
        chinachu = appClass.getChinachu();

        actionbar.setListNavigationCallbacks(spinnerAdapter, this);
    }

    // ActionBarのSpinnerで選択された時呼ばれる
    @Override
    public boolean onNavigationItemSelected(final int itemPosition, long itemId){
        showingChannelId = channelIdList[itemPosition];
        new AsyncTask<Void, Void, Program[]>(){
            private ProgressDialog progDialog;

            @Override
            protected void onPreExecute(){
                progDialog = new ProgressDialog(ChannelScheduleActivity.this);
                progDialog.setMessage("Loading...");
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(true);
                progDialog.show();
            }

            @Override
            protected Program[] doInBackground(Void... params){
                try{
                    return chinachu.getChannelSchedule(showingChannelId);
                }catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Program[] result){
                progDialog.dismiss();
                programListAdapter.clear();
                if(result == null){
                    Toast.makeText(ChannelScheduleActivity.this, "番組取得エラー", Toast.LENGTH_SHORT).show();
                    return;
                }
                Arrays.sort(result, new Comparator<Program>(){
                    @Override
                    public int compare(Program lhs, Program rhs){
                        if(lhs.getStart() > rhs.getStart())
                            return 1;
                        else if(lhs.getStart() < rhs.getStart())
                            return -1;
                        else
                            return 0;
                    }
                });
                programListAdapter.addAll((Object[])result);
                for(int i = 0; i < result.length; i++){
                    long start = result[i].getStart();
                    long end = result[i].getEnd();
                    long now = new Date().getTime();
                    if(start < now && end > now){
                        programList.setSelection(i);
                        break;
                    }
                }
            }
        }.execute();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(appClass.getStreaming())
            menu.add(0, Menu.FIRST, Menu.NONE, "ライブ再生");
        if(appClass.getEncStreaming())
            menu.add(0, Menu.FIRST + 1, Menu.NONE, "ライブ再生(エンコ有)");

        getMenuInflater().inflate(R.menu.search, menu);
        searchView = (SearchView)menu.findItem(R.id.search_view).getActionView();
        searchView.setQueryHint("全チャンネルから番組検索");
        searchView.setOnQueryTextListener(new OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query){
                Intent i = new Intent(ChannelScheduleActivity.this, ProgramActivity.class);
                i.putExtra("type", 5);
                i.putExtra("query", query);
                startActivity(i);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
        if(item.getItemId() == Menu.FIRST || item.getItemId() == Menu.FIRST + 1){
            String nowProgramTitle = null;
            for(int i = 0; i < programListAdapter.getCount(); i++){
                Program program = (Program)programListAdapter.getItem(i);
                long start = program.getStart();
                long end = program.getEnd();
                long now = new Date().getTime();
                if(start < now && end > now){
                    nowProgramTitle = program.getTitle();
                    break;
                }
            }
            String title = item.getItemId() == Menu.FIRST ? "ライブ視聴しますか？" : "エンコ有ライブ視聴しますか？";
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage("放送中：" + nowProgramTitle)
                    .setNegativeButton("キャンセル", null)
                    .setPositiveButton("OK", new OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            if(item.getItemId() == Menu.FIRST){
                                Uri uri = Uri.parse(appClass.getChinachu().getNonEncLiveMovieURL(showingChannelId));
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }else{
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
                                Uri uri = Uri.parse(appClass.getChinachu().getEncLiveMovieURL(showingChannelId, t, params));
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }
                    }).show();
        }else if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(!searchView.isIconified()){
            searchView.setIconified(true);
        }else{
            super.onBackPressed();
        }
    }
}