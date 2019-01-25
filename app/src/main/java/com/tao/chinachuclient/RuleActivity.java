package com.tao.chinachuclient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import Chinachu4j.Rule;

public class RuleActivity extends AppCompatActivity implements OnRefreshListener, OnItemClickListener{

    private SwipeRefreshLayout swipeRefresh;
    private RuleListAdapter adapter;
    private ApplicationClass appClass;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);

        appClass = (ApplicationClass)getApplicationContext();

        ListView list = (ListView)findViewById(R.id.programList);
        adapter = new RuleListAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
        swipeRefresh.setOnRefreshListener(this);

        actionBar = getSupportActionBar();
        setActionBarTitle(-1);
        actionBar.setDisplayHomeAsUpEnabled(true);

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
                    progDialog.setMessage(getString(R.string.loading));
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
                if(result == null){
                    Toast.makeText(RuleActivity.this, R.string.error_get_rule, Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.addAll(result);
                setActionBarTitle(result.length);
            }
        }.execute();
    }

    public void setActionBarTitle(int ruleCount){
        String title = getString(R.string.rule);
        if(ruleCount >= 0){
            title += " (" + ruleCount + ")";
        }
        actionBar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
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
