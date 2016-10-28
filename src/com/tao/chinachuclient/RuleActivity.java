package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import Chinachu4j.Rule;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class RuleActivity extends Activity implements OnRefreshListener, OnItemClickListener{

	private ListView list;
	private SwipeRefreshLayout swipeRefresh;
	private RuleListAdapter adapter;
	private ApplicationClass appClass;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program);

		appClass = (ApplicationClass)getApplicationContext();

		list = (ListView)findViewById(R.id.programList);
		adapter = new RuleListAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		actionBar = getActionBar();
		actionBar.setTitle("ルール");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		asyncLoad(false);
	}

	public void asyncLoad(final boolean isRefresh){
		adapter.clear();
		new AsyncTask<Void, Void, Rule[]>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				if(!isRefresh){
					progDialog = new ProgressDialog(RuleActivity.this);
					progDialog.setMessage("Loading...");
					progDialog.setIndeterminate(false);
					progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progDialog.setCancelable(true);
					progDialog.show();
				}
			}

			@Override
			protected Rule[] doInBackground(Void... params){
				try{
					return appClass.getChinachu().getRules();
				}catch(KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Rule[] result){
				if(!isRefresh)
					progDialog.dismiss();
				else
					swipeRefresh.setRefreshing(false);
				if(result == null) {
					Toast.makeText(RuleActivity.this, "ルール取得エラー", Toast.LENGTH_SHORT).show();
					return;
				}
				adapter.addAll(result);
				setActionBarCount(result.length);
			}
		}.execute();
	}

	public void setActionBarCount(int length){
		actionBar.setTitle("ルール (" + String.valueOf(length) + ")");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRefresh(){
		asyncLoad(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		Rule rule = (Rule)parent.getItemAtPosition(position);
		Intent i = new Intent(this, RuleDetail.class);
		i.putExtra("position", String.valueOf(position));
		i.putExtra("rule", rule);
		startActivity(i);
	}
}
