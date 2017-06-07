package com.tao.chinachuclient;

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
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

public class ProgramDetail extends Activity{

	private Program program;
	private int type;
	private ApplicationClass appClass;
	private String capture;
	private int randomSecond;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_program_detail);

		appClass = (ApplicationClass)getApplicationContext();
		type = getIntent().getIntExtra("type", -1);

		if(type == Type.RESERVES){
			Reserve reserve = (Reserve)getIntent().getSerializableExtra("reserve");
			program = reserve.getProgram();
		}else if(type == Type.RECORDED){
			Recorded recorded = (Recorded)getIntent().getSerializableExtra("recorded");
			program = recorded.getProgram();
		}else{
			program = (Program)getIntent().getSerializableExtra("program");
		}

		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(false);
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

		TextView detailView = (TextView)findViewById(R.id.program_detail_detail);
		detail = detail.replace("\n", "<br />");
		detailView.setMovementMethod(LinkMovementMethod.getInstance());
		Matcher m = Pattern.compile("http(s)?://[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+").matcher(detail);
		while(m.find())
			detail = detail.replace(m.group(), String.format("<a href=\"%s\">%s</a>", m.group(), m.group()));
		String detailText = "<p><strong>フルタイトル</strong><br />" + program.getFullTitle() + "<br /></p><p><strong>詳細</strong><br />" + detail + "</p>";
		detailView.setText(Html.fromHtml(detailText));

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
				+ ": " + channelName + "<br /><br />フラグ：" + flag + "<br /><br />id：" + program.getId() + "</p>";
		otherView.setText(Html.fromHtml(otherText));

		if(type == Type.RECORDING || type == Type.RECORDED) {
			new AsyncTask<Void, Void, String>(){
				@Override
				protected String doInBackground(Void... params){
					try{
						if(type == Type.RECORDING)
							return appClass.getChinachu().getRecordingImage(program.getId(), "1280x720");
						if(type == Type.RECORDED) {
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
					if(result == null) {
						Toast.makeText(ProgramDetail.this, "画像取得エラー", Toast.LENGTH_SHORT).show();
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
		.setNegativeButton("キャンセル", null)
		.setPositiveButton("OK", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				new AsyncTask<Void, Void, String>(){
					private ProgressDialog progDialog;

					@Override
					protected void onPreExecute(){
						progDialog = new ProgressDialog(ProgramDetail.this);
						progDialog.setMessage("Loading...");
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
							Toast.makeText(ProgramDetail.this, "画像の取得に失敗しました", Toast.LENGTH_SHORT).show();
							return;
						}
						if(result.startsWith("data:image/jpeg;base64,"))
							result = result.substring(23);
						Intent i = new Intent(ProgramDetail.this, Show_Image.class);
						i.putExtra("base64", result);
						i.putExtra("programId", program.getId());
						if(type == Type.RECORDED)
							i.putExtra("pos", Integer.parseInt(cap_pos.getText().toString()));
						startActivity(i);
					}
				}.execute();
			}
		}).setNeutralButton("このまま拡大", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				Intent i = new Intent(ProgramDetail.this, Show_Image.class);
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
			menu.add(0, Menu.FIRST, Menu.NONE, "予約");
		}else if(type == Type.RESERVES){
			menu.add(0, Menu.FIRST + 1, Menu.NONE, "予約削除");
		}else if(type == Type.RECORDING || type == Type.RECORDED){
			if(appClass.getStreaming())
				menu.add(0, Menu.FIRST + 2, Menu.NONE, "ストリーミング再生");
			if(appClass.getEncStreaming())
				menu.add(0, Menu.FIRST + 3, Menu.NONE, "ストリーミング再生(エンコ有)");
		}
		if(type == Type.RECORDED){
			menu.add(0, Menu.FIRST + 4, Menu.NONE, "録画ファイル削除");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home){
			finish();
		}else if(item.getItemId() == Menu.FIRST + 2) {
			if(type == Type.RECORDING) {
				Uri uri = Uri.parse(appClass.getChinachu().getNonEncRecordingMovieURL(program.getId()));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			if(type == Type.RECORDED) {
				Uri uri = Uri.parse(appClass.getChinachu().getNonEncRecordedMovieURL(program.getId()));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		}else if(item.getItemId() == Menu.FIRST + 3) {
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
			if(type == Type.RECORDING) {
				Uri uri = Uri.parse(appClass.getChinachu().getEncRecordingMovieURL(program.getId(), t, params));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			if(type == Type.RECORDED) {
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
			before.setTitle("予約しますか？");
			break;
		case Type.RESERVES:
			before.setTitle("予約を削除しますか？");
			break;
		case Type.RECORDED:
			before.setTitle("録画ファイルを削除しますか？");
			break;
		}
		before.setMessage(program.getFullTitle()).setNegativeButton("キャンセル", null).setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				new AsyncTask<Void, Void, ChinachuResponse>(){
					private ProgressDialog progDialog;

					@Override
					protected void onPreExecute(){
						progDialog = new ProgressDialog(ProgramDetail.this);
						progDialog.setMessage("Sending...");
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
								return chinachu.delReserve(program.getId());
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
						if(result == null) {
							Toast.makeText(ProgramDetail.this, "通信エラー", Toast.LENGTH_SHORT).show();
							return;
						}
						if(!result.getResult()) {
							Toast.makeText(ProgramDetail.this, result.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}

						AlertDialog.Builder after = new Builder(ProgramDetail.this);
						switch(type){
						case Type.CHANNEL_SCHEDULE_ACTIVITY:
						case Type.SEARCH_PROGRAM:
							after.setTitle("予約完了");
							after.setMessage(program.getFullTitle());
							break;
						case Type.RESERVES:
							after.setTitle("予約の削除完了");
							after.setMessage(program.getFullTitle());
							break;
						case Type.RECORDED:
							after.setTitle("録画ファイルの削除完了");
							after.setMessage(program.getFullTitle() + "\n\n録画済みリストへの反映にはクリーンアップが必要です");
							break;
						}
						after.show();
					}
				}.execute();
			}
		}).show();
	}
}