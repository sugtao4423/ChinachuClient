package com.tao.chinachuclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tenthbit.view.ZoomImageView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Show_Image extends AppCompatActivity{

	private String programId;
	private int pos;
	private byte[] byteImage;
	private ZoomImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image);
		image = (ZoomImageView)findViewById(R.id.show_image_image);

		Intent i = getIntent();
		final String base64 = i.getStringExtra("base64");
		pos = i.getIntExtra("pos", -1);
		programId = i.getStringExtra("programId");
		byteImage = Base64.decode(base64, Base64.DEFAULT);

		image.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
	}

	public void image_option_click(View v){
		new AlertDialog.Builder(this)
		.setMessage("保存しますか？")
		.setNegativeButton("キャンセル", null)
		.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				saveImage();
			}
		}).show();
	}

	public void saveImage(){
		final String fileName;
		if(pos == -1)
			fileName = programId;
		else
			fileName = programId + "-" + pos;
		final String type = ".jpg";
		final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
		final String imgPath = saveDir + "/" + fileName + type;

		if(new File(imgPath).exists()) {
			new AlertDialog.Builder(this)
			.setTitle("エラー:ファイルが既に存在しています")
			.setItems(new String[]{"上書き", "ファイル名を指定して保存", "キャンセル"}, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					if(which == 0) {
						save(imgPath);
					}else if(which == 1) {
						final EditText edit = new EditText(Show_Image.this);
						edit.setText(fileName);
						new AlertDialog.Builder(Show_Image.this)
						.setTitle("ファイル名を指定してください")
						.setView(edit)
						.setNegativeButton("キャンセル", null)
						.setPositiveButton("OK", new OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which){
								String newPath = saveDir + "/" + edit.getText().toString() + type;
								if(new File(newPath).exists())
									saveImage();
								else
									save(newPath);
							}
						}).show();
					}
				}
			}).show();
		}else{
			save(imgPath);
		}
	}

	public void save(String imgPath){
		FileOutputStream fos;
		try{
			fos = new FileOutputStream(imgPath, true);
			fos.write(byteImage);
			fos.close();
		}catch(IOException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		Toast.makeText(this, "保存しました\n" + imgPath, Toast.LENGTH_LONG).show();
	}
}