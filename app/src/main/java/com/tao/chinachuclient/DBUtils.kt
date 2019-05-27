package com.tao.chinachuclient

import android.content.Context
import android.database.Cursor
import android.preference.PreferenceManager

class DBUtils(private val context: Context) {

    private val db = ServerSQLHelper(context).writableDatabase

    fun getServers(): Array<Server> {
        val servers = arrayListOf<Server>()
        val c = db.rawQuery("SELECT * FROM servers", null)
        while (c.moveToNext()) {
            servers.add(getServer(c))
        }
        c.close()
        return servers.toTypedArray()
    }

    fun getServerFromAddress(chinachuAddress: String): Server {
        val c = db.rawQuery("SELECT * FROM servers WHERE chinachuAddress = ?", arrayOf(chinachuAddress))
        c.moveToNext()
        val server = getServer(c)
        c.close()
        return server
    }

    private fun getServer(c: Cursor): Server {
        c.apply {
            val chinachuAddress = getString(0)
            val username = getString(1)
            val password = getString(2)
            val streaming = getString(3).run { toBoolean() }
            val encStreaming = getString(4).run { toBoolean() }
            val type = getString(5)
            val containerFormat = getString(6)
            val videoCodec = getString(7)
            val audioCodec = getString(8)
            val videoBitrate = getString(9)
            val audioBitrate = getString(10)
            val videoSize = getString(11)
            val frame = getString(12)
            val channelIds = getString(13)
            val channelNames = getString(14)
            val oldCategoryColor = getString(15).run { toBoolean() }

            val encode = Encode(type, containerFormat, videoCodec, audioCodec, videoBitrate, audioBitrate, videoSize, frame)
            return Server(chinachuAddress, username, password, streaming, encStreaming, encode, channelIds, channelNames, oldCategoryColor)
        }
    }

    fun insertServer(server: Server) {
        val sql = "INSERT INTO servers VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        val bindArgs = server.run {
            arrayOf(
                    chinachuAddress,
                    username,
                    password,
                    streaming.toString(),
                    encStreaming.toString(),
                    encode.type,
                    encode.containerFormat,
                    encode.videoCodec,
                    encode.audioCodec,
                    encode.videoBitrate,
                    encode.audioBitrate,
                    encode.videoSize,
                    encode.frame,
                    channelIds,
                    channelNames,
                    oldCategoryColor.toString()
            )
        }
        db.compileStatement(sql).apply {
            bindAllArgsAsStrings(bindArgs)
            execute()
            close()
        }
    }

    fun updateServer(server: Server, targetChinachuAddress: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val sql = "UPDATE servers SET chinachuAddress = ?, username = ?, password = ?, streaming = ?, encStreaming = ?," +
                "type = ?, containerFormat = ?, videoCodec = ?, audioCodec = ?, videoBitrate = ?, audioBitrate = ?," +
                "videoSize = ?, frame = ?, oldCategoryColor = ? WHERE chinachuAddress = ?"
        val bindArgs = server.run {
            arrayOf(
                    chinachuAddress,
                    username,
                    password,
                    pref.getBoolean("streaming", false).toString(),
                    pref.getBoolean("encStreaming", false).toString(),
                    encode.type,
                    encode.containerFormat,
                    encode.videoCodec,
                    encode.audioCodec,
                    encode.videoBitrate,
                    encode.audioBitrate,
                    encode.videoSize,
                    encode.frame,
                    pref.getBoolean("oldCategoryColor", false).toString(),
                    targetChinachuAddress
            )
        }
        db.compileStatement(sql).apply {
            bindAllArgsAsStrings(bindArgs)
            execute()
            close()
        }
    }

    fun serverExists(chinachuAddress: String): Boolean {
        val c = db.rawQuery("SELECT * FROM servers WHERE chinachuAddress = ?", arrayOf(chinachuAddress))
        c.moveToFirst()
        val count = c.count
        c.close()
        return count > 0
    }

    fun close() {
        db.close()
    }

    fun serverPutPref(server: Server) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putString("chinachuAddress", server.chinachuAddress)
            putString("username", server.username)
            putString("password", server.password)
            putBoolean("streaming", server.streaming)
            putBoolean("encStreaming", server.encStreaming)
            putBoolean("oldCategoryColor", server.oldCategoryColor)
            commit()
        }

        val encode = server.encode.run {
            arrayOf(
                    type,
                    containerFormat,
                    videoCodec,
                    audioCodec,
                    videoBitrate,
                    audioBitrate,
                    videoSize,
                    frame
            )
        }

        context.getSharedPreferences("encodeConfig", Context.MODE_PRIVATE).edit().apply {
            putString("type", encode[0])
            putString("containerFormat", encode[1])
            putString("videoCodec", encode[2])
            putString("audioCodec", encode[3])
            putString("videoBitrate", encode[4])
            putString("audioBitrate", encode[5])
            putString("videoSize", encode[6])
            putString("frame", encode[7])
            commit()
        }

        context.getSharedPreferences("channels", Context.MODE_PRIVATE).edit().apply {
            putString("channelIds", server.channelIds)
            putString("channelNames", server.channelNames)
            commit()
        }
    }

}
