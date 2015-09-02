package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import Chinachu4j.Chinachu4j;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProgramDetail extends Activity{

	private String programTitle, programId;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program_detail);

		Intent i = getIntent();
		programTitle = i.getStringExtra("title");
		getActionBar().setTitle(programTitle);
		programId = i.getStringExtra("id");
		String fullTitle = i.getStringExtra("fullTitle");
		String detail = i.getStringExtra("detail");
		long start = i.getLongExtra("start", -1);
		long end = i.getLongExtra("end", -1);
		final int seconds = i.getIntExtra("seconds", -1);
		String category = i.getStringExtra("category");
		String[] flags = i.getStringArrayExtra("flags");
		String channelType = i.getStringExtra("channelType");
		String channelName = i.getStringExtra("channelName");
		type = i.getIntExtra("type", -1);

		final ImageView image = (ImageView)findViewById(R.id.programs_detail_image);
		if(type == 2 || type == 3)
			image.setVisibility(View.VISIBLE);
		else
			image.setVisibility(View.GONE);

		TextView detailView = (TextView)findViewById(R.id.program_detail_detail);
		String detailText = "<p><strong>フルタイトル</strong><br />" + fullTitle + "<br /></p><p><strong>詳細</strong><br />"
				+ detail + "</p>";
		detailView.setText(Html.fromHtml(detailText));

		TextView otherView = (TextView)findViewById(R.id.program_detail_other);

		String startStr = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPANESE).format(new Date(start));
		String endStr = new SimpleDateFormat("HH:mm", Locale.JAPANESE).format(new Date(end));
		String minute = seconds / 60 + "分間";
		String flag = "";
		for(String s : flags)
			flag += s + ", ";
		if(flag.length() == 0)
			flag = "なし";
		else
			flag = flag.substring(0, flag.length() - 2);
		String otherText =
				"<p>" + startStr + " 〜 " + endStr + " (" + minute + ")<br /><br />" + category + " / " + channelType
						+ ": " + channelName + "<br /><br />フラッグ：" + flag + "<br /><br />id：" + programId + "</p>";
		otherView.setText(Html.fromHtml(otherText));

		if(type == 2 || type == 3) {
			AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
				@Override
				protected String doInBackground(Void... params){
					try{
						if(type == 2)
							return ((ApplicationClass)getApplicationContext()).getChinachu()
									.getRecordingImage(programId, "1280x720");
						if(type == 3) {
							int r = new Random().nextInt(seconds) + 1;
							return ((ApplicationClass)getApplicationContext()).getChinachu().getRecordedImage(programId,
									r, "1280x720");
						}
						return null;
					}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
						return null;
					}
				}

				@Override
				protected void onPostExecute(String result){
					if(result == null) {
						Toast.makeText(ProgramDetail.this, "画像取得エラー", Toast.LENGTH_SHORT).show();
						return;
					}
					if(result.startsWith("data:image/jpeg;base64,"))
						result = result.substring(23);
					byte[] decodedString = Base64.decode(result, Base64.DEFAULT);
					Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
					image.setImageBitmap(bmp);
				}
			};
			task.execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(type == 0)
			menu.add("予約");
		if(type == 1)
			menu.add("予約削除");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Confirm();
		return super.onOptionsItemSelected(item);
	}

	public void Confirm(){
		final Chinachu4j chinachu = ((ApplicationClass)getApplicationContext()).getChinachu();

		AlertDialog.Builder before = new Builder(this);
		switch(type){
		case 0:
			before.setTitle("予約しますか？");
			break;
		case 1:
			before.setTitle("予約を削除しますか？");
			break;
		}
		before.setMessage(programTitle).setNegativeButton("キャンセル", null).setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
					private ProgressDialog progDailog;

					@Override
					protected void onPreExecute(){
						progDailog = new ProgressDialog(ProgramDetail.this);
						progDailog.setMessage("Sending...");
						progDailog.setIndeterminate(false);
						progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progDailog.setCancelable(true);
						progDailog.show();
					}

					@Override
					protected Boolean doInBackground(Void... params){
						try{
							switch(type){
							case 0:
								chinachu.putReserve(programId);
								break;
							case 1:
								chinachu.delReserve(programId);
								break;
							}
							return true;
						}catch(KeyManagementException | NoSuchAlgorithmException | IOException e){
							return false;
						}
					}

					@Override
					protected void onPostExecute(Boolean result){
						progDailog.dismiss();
						if(!result){
							Toast.makeText(ProgramDetail.this, "エラー", Toast.LENGTH_SHORT).show();
							return;
						}
						AlertDialog.Builder after = new Builder(ProgramDetail.this);
						switch(type){
						case 0:
							after.setTitle("予約完了");
							break;
						case 1:
							after.setTitle("予約の削除完了");
							break;
						}
						after.setMessage(programTitle)
						.create().show();
					}
				};
				task.execute();
			}
		});
		before.create().show();
	}
}
