package com.tao.chinachuclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import Chinachu4j.ChinachuResponse;
import Chinachu4j.Rule;

public class RuleDetail extends AppCompatActivity{

    private String reserve_title;
    private String position;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);

        Intent i = getIntent();
        position = i.getStringExtra("position");
        Rule tmp = (Rule)i.getSerializableExtra("rule");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(tmp.getReserve_titles().length > 0 ? tmp.getReserve_titles()[0] : "any");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TextView textView = (TextView)findViewById(R.id.rule_list_text);

        String type = tmp.getTypes().length > 0 ? array2string(tmp.getTypes()) : "any";
        String category = tmp.getCategories().length > 0 ? array2string(tmp.getCategories()) : "any";
        String channel = tmp.getChannels().length > 0 ? array2string(tmp.getChannels()) : "any";
        String ignore_channel = tmp.getIgnore_channels().length > 0 ? array2string(tmp.getIgnore_channels()) : "none";
        String reserve_flag = tmp.getReserve_flags().length > 0 ? array2string(tmp.getReserve_flags()) : "any";
        String ignore_flag = tmp.getIgnore_flags().length > 0 ? array2string(tmp.getIgnore_flags()) : "none";

        String start_end = (tmp.getStart() == -1 ? 0 : tmp.getStart()) + "〜" + (tmp.getEnd() == -1 ? 0 : tmp.getEnd());
        String min_max = tmp.getMin() == -1 && tmp.getMax() == -1 ? "all" : String.valueOf((tmp.getMin() / 60) + "〜" + (tmp.getMax() / 60));

        reserve_title = tmp.getReserve_titles().length > 0 ? array2string(tmp.getReserve_titles()) : "any";
        String ignore_title = tmp.getIgnore_titles().length > 0 ? array2string(tmp.getIgnore_titles()) : "none";
        String reserve_description = tmp.getReserve_descriptions().length > 0 ? array2string(tmp.getReserve_descriptions()) : "any";
        String ignore_description = tmp.getIgnore_descriptions().length > 0 ? array2string(tmp.getIgnore_descriptions()) : "none";
        String recorded_format = tmp.getRecorded_format() == null || tmp.getRecorded_format().equals("") ? "default" : tmp.getRecorded_format();
        String isDisabled = tmp.getIsDisabled() ? "無効" : "有効";

        String txt = "タイプ： " + type + "<br /><br />ジャンル：" + category + "<br /><br />対象CH：" + channel +
                "<br /><br />無視CH：" + ignore_channel + "<br /><br />対象フラグ：" + reserve_flag +
                "<br /><br />無視フラグ：" + ignore_flag + "<br /><br />時間帯：" + start_end +
                "<br /><br />長さ(分)：" + min_max + "<br /><br />対象タイトル：" + reserve_title +
                "<br /><br />無視タイトル：" + ignore_title + "<br /><br />対象説明文：" + reserve_description +
                "<br /><br />無視説明文：" + ignore_description + "<br /><br />録画ファイル名フォーマット：" +
                recorded_format + "<br /><br />ルールの状態：" + isDisabled;

        textView.setText(Html.fromHtml(txt));
    }

    public String array2string(String[] arr){
        String result = "";
        for(String s : arr)
            result += s + ", ";
        result = result.substring(0, result.length() - 2);
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, Menu.FIRST, Menu.NONE, "ルール削除");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }else if(item.getItemId() == Menu.FIRST){
            new AlertDialog.Builder(this)
                    .setTitle("削除しますか？")
                    .setMessage("ルールNo." + position + "\n対象タイトル：" + reserve_title)
                    .setNegativeButton("キャンセル", null)
                    .setPositiveButton("OK", new OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            new AsyncTask<Void, Void, ChinachuResponse>(){
                                private ProgressDialog progDialog;

                                @Override
                                protected void onPreExecute(){
                                    progDialog = new ProgressDialog(RuleDetail.this);
                                    progDialog.setMessage("Sending...");
                                    progDialog.setIndeterminate(false);
                                    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progDialog.setCancelable(true);
                                    progDialog.show();
                                }

                                @Override
                                protected ChinachuResponse doInBackground(Void... params){
                                    try{
                                        return ((ApplicationClass)getApplicationContext()).getChinachu().delRule(position);
                                    }catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
                                        return null;
                                    }
                                }

                                @Override
                                protected void onPostExecute(ChinachuResponse result){
                                    progDialog.dismiss();
                                    if(result == null){
                                        Toast.makeText(RuleDetail.this, "通信エラー", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if(!result.getResult()){
                                        Toast.makeText(RuleDetail.this, result.getMessage(), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    new AlertDialog.Builder(RuleDetail.this)
                                            .setTitle("削除完了")
                                            .setMessage("削除が完了しました。\n\n前の画面に戻り、リストを上に引っ張るなどでルール一覧を更新してください。")
                                            .setNegativeButton("キャンセル", null)
                                            .setPositiveButton("OK", new OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which){
                                                    finish();
                                                }
                                            }).show();
                                }
                            }.execute();
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
