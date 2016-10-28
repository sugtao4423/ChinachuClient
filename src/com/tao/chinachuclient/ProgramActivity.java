package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.json.JSONException;

import Chinachu4j.ChinachuResponse;
import Chinachu4j.Program;
import Chinachu4j.Recorded;
import Chinachu4j.Reserve;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class ProgramActivity extends Activity implements OnRefreshListener{

	private ListView list;
	private SwipeRefreshLayout swipeRefresh;
	private ProgramListAdapter programListAdapter;
	private ApplicationClass appClass;
	private ActionBar actionbar;

	private int type;
	private String query;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program);

		list = (ListView)findViewById(R.id.programList);
		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);

		actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(false);

		programListAdapter = new ProgramListAdapter(this);
		list.setAdapter(programListAdapter);

		appClass = (ApplicationClass)getApplicationContext();
		// type 1: ルール 2: 予約済み 3: 録画中 4: 録画済み 5: 番組検索
		Intent intent = getIntent();
		type = intent.getIntExtra("type", -1);
		if(type == -1)
			finish();
		else if(type == 5)
			query = intent.getStringExtra("query");

		list.setOnItemClickListener(new ProgramListClickListener(this, type));
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		switch(type){
		case 2:
			actionbar.setTitle("予約済み");
			break;
		case 3:
			actionbar.setTitle("録画中");
			break;
		case 4:
			actionbar.setTitle("録画済み");
			break;
		case 5:
			actionbar.setTitle("検索結果");
			break;
		}

		asyncLoad(false);
	}

	public void asyncLoad(final boolean isRefresh){
		programListAdapter.clear();
		new AsyncTask<Void, Void, Program[]>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				if(!isRefresh){
					progDialog = new ProgressDialog(ProgramActivity.this);
					progDialog.setMessage("Loading...");
					progDialog.setIndeterminate(false);
					progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progDialog.setCancelable(true);
					progDialog.show();
				}
			}

			@Override
			protected Program[] doInBackground(Void... params){
				return load();
			}

			@Override
			protected void onPostExecute(Program[] result){
				if(!isRefresh)
					progDialog.dismiss();
				else
					swipeRefresh.setRefreshing(false);
				if(result == null) {
					Toast.makeText(ProgramActivity.this, "番組取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				programListAdapter.addAll(result);
				setActionBarCount(result.length);
			}
		}.execute();
	}

	public Program[] load(){
		try{
			if(type == 2) {
				Reserve[] reserve = appClass.getChinachu().getReserves();
				Program[] program = new Program[reserve.length];
				for(int i = 0; i < reserve.length; i++)
					program[i] = reserve[i].getProgram();
				return program;
			}
			if(type == 3)
				return appClass.getChinachu().getRecording();
			if(type == 4) {
				Recorded[] recorded = appClass.getChinachu().getRecorded();
				Program[] programs = new Program[recorded.length];
				for(int i = 0; i < recorded.length; i++)
					programs[recorded.length - i - 1] = recorded[i].getProgram();
				return programs;
			}
			if(type == 5) {
				return appClass.getChinachu().searchProgram(query);
			}
			return null;
		}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
			return null;
		}
	}

	public void setActionBarCount(int length){
		String title;
		String len = String.valueOf(length);
		switch(type){
		case 2:
			title = "予約済み (" + len + ")";
			break;
		case 3:
			title = "録画中 (" + len + ")";
			break;
		case 4:
			title = "録画済み (" + len + ")";
			break;
		case 5:
			title = "番組検索 (" + len + ")";
			break;
		default:
			title = "";
			break;
		}
		actionbar.setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(type == 4) {
			MenuItem cleanUp = menu.add("クリーンアップ");
			cleanUp.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		if(item.getTitle().equals("クリーンアップ")) {
			new AlertDialog.Builder(this)
			.setTitle("クリーンアップ")
			.setMessage("録画済みリストをクリーンアップしますか？")
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					new AsyncTask<Void, Void, ChinachuResponse>(){
						private ProgressDialog progDialog;

						@Override
						protected void onPreExecute(){
							progDialog = new ProgressDialog(ProgramActivity.this);
							progDialog.setMessage("Loading...");
							progDialog.setIndeterminate(false);
							progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							progDialog.setCancelable(true);
							progDialog.show();
						}

						@Override
						protected ChinachuResponse doInBackground(Void... params){
							try{
								return appClass.getChinachu().recordedCleanUp();
							}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
								return null;
							}
						}

						@Override
						protected void onPostExecute(ChinachuResponse result){
							progDialog.dismiss();
							if(result == null) {
								Toast.makeText(ProgramActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
								return;
							}
							if(!result.getResult()){
								Toast.makeText(ProgramActivity.this, result.getMessage(), Toast.LENGTH_LONG).show();
								return;
							}

							new AlertDialog.Builder(ProgramActivity.this)
							.setTitle("完了")
							.setMessage("クリーンアップに成功しました\n更新しますか？")
							.setNegativeButton("キャンセル", null)
							.setPositiveButton("OK", new OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog, int which){
									onRefresh();
								}

							}).show();
						}
					}.execute();
				}
			}).show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRefresh(){
		asyncLoad(true);
	}
}