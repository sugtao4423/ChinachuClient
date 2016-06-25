package com.tao.chinachuclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tenthbit.view.ZoomImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

public class Show_Image extends Activity{
	
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
		AlertDialog.Builder save = new AlertDialog.Builder(this);
		save.setMessage("保存しますか？")
		.setNegativeButton("キャンセル", null)
		.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				saveImage();
			}
		});
		save.create().show();
	}
	
	public void saveImage(){
		String fileName;
		if(pos == -1)
			fileName = programId;
		else
			fileName = programId + "-" + pos;
		String type = ".jpg";
		final String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
		final String imgPath = saveDir + "/" + fileName + type;

		if(new File(imgPath).exists()) {
			final String newPath;
			String title;
			int i = 2;
			while(true){
				if(new File(saveDir + "/" + fileName +  "_" + i + type).exists()){
					i++;
				}else{
					newPath = saveDir + "/" + fileName + "_" + i + type;
					if(i == 2)
						title = fileName + type + "という名前のファイルが既に存在しています";
					else
						title = fileName + "_" + (i - 1) + type + "という名前のファイルが既に存在しています";
					break;
				}
			}
			AlertDialog.Builder exists = new AlertDialog.Builder(this);
			exists.setTitle(title)
			.setItems(new String[]{"上書き", "_" + i + "をつけて保存", "キャンセル"}, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					if(which == 0)
						save(imgPath);
					if(which == 1)
						save(newPath);
				}
			});
			exists.create().show();
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