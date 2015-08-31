package com.tao.chinachuclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProgramDetail extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program_detail);
		
		Intent i = getIntent();
		getActionBar().setTitle(i.getStringExtra("title"));
		final String id = i.getStringExtra("id");
		String fullTitle = i.getStringExtra("fullTitle");
		String detail = i.getStringExtra("detail");
		long start = i.getLongExtra("start", -1);
		long end = i.getLongExtra("end", -1);
		final int seconds = i.getIntExtra("seconds", -1);
		String category = i.getStringExtra("category");
		String[] flags = i.getStringArrayExtra("flags");
		String channelType = i.getStringExtra("channelType");
		String channelName = i.getStringExtra("channelName");
		final int type = i.getIntExtra("type", -1);
		
		final ImageView image = (ImageView)findViewById(R.id.programs_detail_image);
		if(type == 2 || type == 3)
			image.setVisibility(View.VISIBLE);
		else
			image.setVisibility(View.GONE);
		
		TextView detailView = (TextView)findViewById(R.id.program_detail_detail);
		String detailText = "<p><strong>フルタイトル</strong><br />" + fullTitle +
				"<br /></p><p><strong>詳細</strong><br />" + detail + "</p>";
		detailView.setText(Html.fromHtml(detailText));
		
		TextView otherView = (TextView)findViewById(R.id.program_detail_other);
		
		String startStr = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(start));
		String endStr = new SimpleDateFormat("HH:mm").format(new Date(end));
		String minute = seconds / 60 + "分間";
		String flag = "";
		for(String s : flags)
			flag += s + ", ";
		if(flag.length() == 0)
			flag = "なし";
		else
			flag = flag.substring(0, flag.length() - 2);
		String otherText = "<p>" + startStr + " 〜 " + endStr + " (" + minute + ")<br /><br />" +
			category + " / " + channelType + ": " + channelName + "<br /><br />フラッグ：" + flag + "<br /><br />id："
					+ id + "</p>";
		otherView.setText(Html.fromHtml(otherText));
		
		if(type == 2 || type == 3){
			AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					try {
						if(type == 2)
							return ((ApplicationClass)getApplicationContext()).getChinachu().getRecordingImage(id, "1280x720");
						if(type == 3){
							int r = new Random().nextInt(seconds) + 1;
							return ((ApplicationClass)getApplicationContext()).getChinachu().getRecordedImage(id, r, "1280x720");
						}
						return null;
					} catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
						return null;
					}
				}
				@Override
				protected void onPostExecute(String result){
					if(result == null){
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
}
