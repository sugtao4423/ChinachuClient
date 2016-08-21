package com.tao.chinachuclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class ServerSQLHelper extends SQLiteOpenHelper{

	private Context context;

	public ServerSQLHelper(Context context){
		super(context, "servers", null, 3);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table servers(chinachuAddress, username, password, streaming, encStreaming, "
				+ "type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame, channelIds, channelNames, oldCategoryColor)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if(oldVersion < 3 && newVersion == 3){
			db.execSQL("drop table servers");
			onCreate(db);
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			pref.edit().clear().commit();
		}
	}
}