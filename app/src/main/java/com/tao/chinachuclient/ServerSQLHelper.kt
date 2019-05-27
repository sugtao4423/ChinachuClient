package com.tao.chinachuclient

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.preference.PreferenceManager

class ServerSQLHelper(private val context: Context) : SQLiteOpenHelper(context, "servers", null, 3) {

    override fun onCreate(db: SQLiteDatabase?) {
        val tableNames = arrayOf(
                "chinachuAddress",
                "username",
                "password",
                "streaming",
                "encStreaming",
                "type",
                "containerFormat",
                "videoCodec",
                "audioCodec",
                "videoBitrate",
                "audioBitrate",
                "videoSize",
                "frame",
                "channelIds",
                "channelNames",
                "oldCategoryColor"
        )
        val commaSeparated = tableNames.joinToString()
        db?.execSQL("CREATE TABLE servers($commaSeparated)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3 && newVersion == 3) {
            db?.execSQL("DROP TABLE servers")
            onCreate(db)
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        }
    }
}