package com.tao.chinachuclient;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ServerSQLHelper extends SQLiteOpenHelper{

	public ServerSQLHelper(Context context){
		super(context, "servers", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table servers(chinachuAddress, username, password, streaming, encStreaming, "
				+ "type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame, channelIds, channelNames, oldCategoryColor)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if(oldVersion == 1 && newVersion == 2){
			db.execSQL("alter table servers add column oldCategoryColor");

			ArrayList<String> arr = new ArrayList<String>();
			Cursor result = db.rawQuery("select * from servers", null);
			boolean mov = result.moveToFirst();
			while(mov){
				arr.add(result.getString(0));
				mov = result.moveToNext();
			}
			for(String s : arr)
				db.execSQL("update servers set oldCategoryColor='false' where chinachuAddress='" + s + "'");
		}
	}
}