package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import Chinachu4j.Rule;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class RuleDetail extends Activity{
	
	private String type, category, channel, ignore_channel, reserve_flag, ignore_flag;
	private String start_end, min_max;
	private String reserve_title, ignore_title, reserve_description, ignore_description;
	private String recorded_format, isDisabled;
	
	private TextView textView;
	
	private String position;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule_detail);
		
		position = getIntent().getStringExtra("position");
		
		Rule tmp = ((ApplicationClass)getApplicationContext()).getTmpRule();
		
		ActionBar actionBar = getActionBar();
		if(tmp.getReserve_titles().length > 0)
			actionBar.setTitle(tmp.getReserve_titles()[0]);
		else
			actionBar.setTitle("any");
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		textView = (TextView)findViewById(R.id.rule_list_text);
		
		if(tmp.getTypes().length > 0)
			type = array2string(tmp.getTypes());
		else
			type = "any";
		
		if(tmp.getCategories().length > 0)
			category= array2string(tmp.getCategories());
		else
			category = "any";
		
		if(tmp.getChannels().length > 0)
			channel = array2string(tmp.getChannels());
		else
			channel = "any";
		
		if(tmp.getIgnore_channels().length > 0)
			ignore_channel = array2string(tmp.getIgnore_channels());
		else
			ignore_channel = "none";
		
		if(tmp.getReserve_flags().length > 0)
			reserve_flag = array2string(tmp.getReserve_flags());
		else
			reserve_flag = "any";
		
		if(tmp.getIgnore_flags().length > 0)
			ignore_flag = array2string(tmp.getIgnore_flags());
		else
			ignore_flag = "none";
		
		int start = tmp.getStart();
		if(start == -1)
			start = 0;
		int end = tmp.getEnd();
		if(end == -1)
			end = 0;
		start_end = start + "〜" + end;
		
		int min = tmp.getMin();
		int max = tmp.getMax();
		if(min == -1 && max == -1)
			min_max = "all";
		else
			min_max = String.valueOf((min / 60) + "〜" + (max / 60));

		if(tmp.getReserve_titles().length > 0)
			reserve_title = array2string(tmp.getReserve_titles());
		else
			reserve_title = "any";
		
		if(tmp.getIgnore_titles().length > 0)
			ignore_title = array2string(tmp.getIgnore_titles());
		else
			ignore_title = "none";
		
		if(tmp.getReserve_descriptions().length > 0)
			reserve_description = array2string(tmp.getReserve_descriptions());
		else
			reserve_description = "any";
		
		if(tmp.getIgnore_descriptions().length > 0)
			ignore_description = array2string(tmp.getIgnore_descriptions());
		else
			ignore_description = "none";
		
		if(recorded_format == null)
			recorded_format = "default";
		
		if(tmp.getIsDisabled())
			isDisabled = "無効";
		else
			isDisabled = "有効";
		
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
		menu.add("ルール削除");
		return true;
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home){
				finish();
				return true;
		}
		if(item.getTitle().equals("ルール削除")){
			AlertDialog.Builder confirm = new AlertDialog.Builder(this);
			confirm.setTitle("削除しますか？")
			.setMessage("ルールNo." + position + "\n対象タイトル：" + reserve_title)
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
						private ProgressDialog progDailog;
						@Override
				        protected void onPreExecute() {
				            progDailog = new ProgressDialog(RuleDetail.this);
				            progDailog.setMessage("Sending...");
				            progDailog.setIndeterminate(false);
				            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				            progDailog.setCancelable(true);
				            progDailog.show();
				        }
						@Override
						protected Boolean doInBackground(Void... params){
							try{
								((ApplicationClass)getApplicationContext()).getChinachu().delRule(position);
								return true;
							}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
								return false;
							}
						}
						@Override
						protected void onPostExecute(Boolean result){
							progDailog.dismiss();
							if(!result){
								Toast.makeText(RuleDetail.this, "ルール削除失敗", Toast.LENGTH_SHORT).show();
								return;
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(RuleDetail.this);
							builder.setTitle("削除完了")
							.setMessage("削除が完了しました。\n\n前の画面に戻り、リストを上に引っ張るなどでルール一覧を更新してください。")
							.setNegativeButton("キャンセル", null)
							.setPositiveButton("OK", new OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									finish();
								}
							});
							builder.create().show();
						}
					};
					task.execute();
				}
			});
			confirm.create().show();
		}
		return super.onOptionsItemSelected(item);
	}
}
