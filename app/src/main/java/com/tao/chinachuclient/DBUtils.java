package com.tao.chinachuclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;

import com.tao.chinachuclient.data.Encode;
import com.tao.chinachuclient.data.Server;

import java.util.ArrayList;

public class DBUtils{

    private SQLiteDatabase db;

    public DBUtils(Context context){
        db = new ServerSQLHelper(context).getWritableDatabase();
    }

    public Server[] getServers(){
        ArrayList<Server> result = new ArrayList<Server>();
        Cursor c = db.rawQuery("SELECT * FROM servers", null);
        while(c.moveToNext()){
            Server server = getServer(c);
            result.add(server);
        }
        c.close();
        return (Server[])result.toArray(new Server[0]);
    }

    public Server getServerFromAddress(String chinachuAddress){
        Cursor c = db.rawQuery("SELECT * FROM servers WHERE chinachuAddress = ?", new String[]{chinachuAddress});
        c.moveToNext();
        Server server = getServer(c);
        c.close();
        return server;
    }

    private Server getServer(Cursor c){
        String chinachuAddress = c.getString(0);
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
        Server server = new Server(chinachuAddress, username, password, streaming, encStreaming, encode, channelIds, channelNames, oldCategoryColor);
        return server;
    }

    public void insertServer(Server server){
        String sql = "INSERT INTO servers VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String[] bindArgs = new String[]{
                server.getChinachuAddress(),
                server.getUsername(),
                server.getPassword(),
                String.valueOf(server.getStreaming()),
                String.valueOf(server.getEncStreaming()),
                server.getEncode().getType(),
                server.getEncode().getContainerFormat(),
                server.getEncode().getVideoCodec(),
                server.getEncode().getAudioCodec(),
                server.getEncode().getVideoBitrate(),
                server.getEncode().getAudioBitrate(),
                server.getEncode().getVideoSize(),
                server.getEncode().getFrame(),
                server.getChannelIds(),
                server.getChannelNames(),
                String.valueOf(server.getOldCategoryColor())
        };
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindAllArgsAsStrings(bindArgs);
        stmt.execute();
        stmt.close();
    }

    public void updateServer(Server server, String targetChinachuAddress, Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String sql = "UPDATE servers SET chinachuAddress = ?, username = ?, password = ?, streaming = ?, encStreaming = ?," +
                "type = ?, containerFormat = ?, videoCodec = ?, audioCodec = ?, videoBitrate = ?, audioBitrate = ?," +
                "videoSize = ?, frame = ?, oldCategoryColor = ? WHERE chinachuAddress = ?";
        String[] bindArgs = new String[]{
                server.getChinachuAddress(),
                server.getUsername(),
                server.getPassword(),
                String.valueOf(pref.getBoolean("streaming", false)),
                String.valueOf(pref.getBoolean("encStreaming", false)),
                server.getEncode().getType(),
                server.getEncode().getContainerFormat(),
                server.getEncode().getVideoCodec(),
                server.getEncode().getAudioCodec(),
                server.getEncode().getVideoBitrate(),
                server.getEncode().getAudioBitrate(),
                server.getEncode().getVideoSize(),
                server.getEncode().getFrame(),
                String.valueOf(pref.getBoolean("oldCategoryColor", false)),
                targetChinachuAddress
        };
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindAllArgsAsStrings(bindArgs);
        stmt.execute();
        stmt.close();
    }

    public boolean serverExists(String chinachuAddress){
        Cursor c = db.rawQuery("SELECT * FROM servers WHERE chinachuAddress = ?", new String[]{chinachuAddress});
        c.moveToFirst();
        int count = c.getCount();
        c.close();
        return count > 0;
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
            if(encode[i] != null && (encode[i].isEmpty() || encode[i].equals("null"))){
                encode[i] = null;
            }
        }

        SharedPreferences enc = context.getSharedPreferences("encodeConfig", Context.MODE_PRIVATE);
        enc.edit()
                .putString("type", encode[0])
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
