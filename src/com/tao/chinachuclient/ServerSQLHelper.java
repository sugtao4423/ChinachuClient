package com.tao.chinachuclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ServerSQLHelper extends SQLiteOpenHelper{

	public ServerSQLHelper(Context context){
		super(context, "servers", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table servers(chinachuAddress, username, password, streaming, encStreaming, "
				+ "type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame, channelIds, channelNames)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}
}