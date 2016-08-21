package com.tao.chinachuclient;

import java.util.ArrayList;

import com.tao.chinachuclient.data.Encode;
import com.tao.chinachuclient.data.Server;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

public class DBUtils{

	private SQLiteDatabase db;

	public DBUtils(Context context){
		db = new ServerSQLHelper(context).getWritableDatabase();
	}

	public Server[] getServers(){
		ArrayList<Server> arr = new ArrayList<Server>();
		Cursor result = db.rawQuery("select * from servers", null);
		boolean mov = result.moveToFirst();
		while(mov){
			String chinachuAddress = result.getString(0);
			String username = result.getString(1);
			String password = result.getString(2);
			boolean streaming = Boolean.parseBoolean(result.getString(3));
			boolean encStreaming = Boolean.parseBoolean(result.getString(4));
			String type = result.getString(5);
			String containerFormat = result.getString(6);
			String videoCodec = result.getString(7);
			String audioCodec = result.getString(8);
			String videoBitrate = result.getString(9);
			String audioBitrate = result.getString(10);
			String videoSize = result.getString(11);
			String frame = result.getString(12);
			String channelIds = result.getString(13);
			String channelNames = result.getString(14);
			boolean oldCategoryColor = Boolean.parseBoolean(result.getString(15));

			Encode encode = new Encode(type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame);
			Server server = new Server(chinachuAddress, username, password, streaming, encStreaming, encode, channelIds, channelNames, oldCategoryColor);

			arr.add(server);
			mov = result.moveToNext();
		}
		return (Server[])arr.toArray(new Server[0]);
	}

	public Server getServerFromAddress(String chinachuAddress){
		Cursor c = db.rawQuery("select * from servers where chinachuAddress=?", new String[]{chinachuAddress});
		c.moveToFirst();
		String username = c.getString(1);
		String password = c.getString(2);
		boolean streaming = Boolean.parseBoolean(c.getString(3));
		boolean encStreaming = Boolean.parseBoolean(c.getString(4));
		String type = c.getString(5);
		String containerFormat = c.getString(6);
		String videoCodec = c.getString(7);
		String audioCodec = c.getString(8);
		String videoBitrate = c.getString(9);
		String audioBitrate = c.getString(10);
		String videoSize = c.getString(11);
		String frame = c.getString(12);
		String channelIds = c.getString(13);
		String channelNames = c.getString(14);
		boolean oldCategoryColor = Boolean.parseBoolean(c.getString(15));

		Encode encode = new Encode(type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame);
		return new Server(chinachuAddress, username, password, streaming, encStreaming, encode, channelIds, channelNames, oldCategoryColor);
	}

	public void insertServer(Server server){
		db.execSQL("insert into servers values(" +
				"'" + server.getChinachuAddress() + "', " +
				"'" + server.getUsername() + "', " +
				"'" + server.getPassword() + "', " +
				"'" + server.getStreaming() + "', " +
				"'" + server.getEncStreaming() + "', " +
				"'" + server.getEncode().getType() + "', " +
				"'" + server.getEncode().getContainerFormat() + "', " +
				"'" + server.getEncode().getVideoCodec() + "', " +
				"'" + server.getEncode().getAudioCodec() + "', " +
				"'" + server.getEncode().getVideoBitrate() + "', " +
				"'" + server.getEncode().getAudioBitrate() + "', " +
				"'" + server.getEncode().getVideoSize() + "', " +
				"'" + server.getEncode().getFrame() + "', " +
				"'" + server.getChannelIds() + "', " +
				"'" + server.getChannelNames() + "', " +
				"'" + server.getOldCategoryColor() + "')");
	}

	public void updateServer(Server server, String targetChinachuAddress, Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		db.execSQL("update servers set " +
				"chinachuAddress='" + server.getChinachuAddress() + "', " +
				"username='" + server.getUsername() + "', " +
				"password='" + server.getPassword() + "', " +
				"streaming='" + pref.getBoolean("streaming", false) + "', " +
				"encStreaming='" + pref.getBoolean("encStreaming", false) + "', " +
				"type='" + server.getEncode().getType() + "', " +
				"containerFormat='" + server.getEncode().getContainerFormat() + "', " +
				"videoCodec='" + server.getEncode().getVideoCodec() + "', " +
				"audioCodec='" + server.getEncode().getAudioCodec() + "', " +
				"videoBitrate='" + server.getEncode().getVideoBitrate() + "', " +
				"audioBitrate='" + server.getEncode().getAudioBitrate() + "', " +
				"videoSize='" + server.getEncode().getVideoSize() + "', " +
				"frame='" + server.getEncode().getFrame() + "', " +
				"oldCategoryColor='" + pref.getBoolean("oldCategoryColor", false) + "' " +
				"where chinachuAddress='" + targetChinachuAddress + "'");
	}

	public boolean serverExists(String chinachuAddress){
		Cursor c = db.rawQuery("select * from servers where chinachuAddress=?", new String[]{chinachuAddress});
		c.moveToFirst();
		if(c.getCount() > 0)
			return true;
		else
			return false;
	}

	public void close(){
		db.close();
	}

	public void serverPutPref(Context context, Server server){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		pref.edit()
		.putString("chinachuAddress", server.getChinachuAddress())
		.putString("username", server.getUsername())
		.putString("password", server.getPassword())
		.putBoolean("streaming", server.getStreaming())
		.putBoolean("encStreaming", server.getEncStreaming())
		.putBoolean("oldCategoryColor", server.getOldCategoryColor())
		.commit();

		String[] encode = new String[8];
		encode[0] = server.getEncode().getType();
		encode[1] = server.getEncode().getContainerFormat();
		encode[2] = server.getEncode().getVideoCodec();
		encode[3] = server.getEncode().getAudioCodec();
		encode[4] = server.getEncode().getVideoBitrate();
		encode[5] = server.getEncode().getAudioBitrate();
		encode[6] = server.getEncode().getVideoSize();
		encode[7] = server.getEncode().getFrame();
		for(int i = 0; i < encode.length; i++){
			if(encode[i] != null){
				if(encode[i].isEmpty() || encode[i].equals("null"))
					encode[i] = null;
			}
		}

		SharedPreferences enc = context.getSharedPreferences("encodeConfig", Context.MODE_PRIVATE);
		enc.edit().putString("type", encode[0])
		.putString("containerFormat", encode[1])
		.putString("videoCodec", encode[2])
		.putString("audioCodec", encode[3])
		.putString("videoBitrate", encode[4])
		.putString("audioBitrate", encode[5])
		.putString("videoSize", encode[6])
		.putString("frame", encode[7])
		.commit();

		SharedPreferences channels = context.getSharedPreferences("channels", Context.MODE_PRIVATE);
		channels.edit()
		.putString("channelIds", server.getChannelIds())
		.putString("channelNames", server.getChannelNames())
		.commit();
	}
}
