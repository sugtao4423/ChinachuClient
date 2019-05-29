package com.tao.chinachuclient

import Chinachu4j.Chinachu4j
import android.app.Application
import android.preference.PreferenceManager
import android.util.Base64
import android.widget.EditText
import android.widget.Spinner

class ApplicationClass : Application() {

    lateinit var chinachu: Chinachu4j
    lateinit var currentServer: Server
    var streaming: Boolean = false
    var encStreaming: Boolean = false
    var reloadList: Boolean = false

    fun reloadCurrentServer(){
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val currentChinachuAddress = pref.getString("chinachuAddress", "") ?: ""
        val server = DBUtils(this).getServerFromAddress(currentChinachuAddress)
        changeCurrentServer(server)
    }

    fun changeCurrentServer(newServer: Server) {
        currentServer = newServer
        chinachu = Chinachu4j(newServer.chinachuAddress,
                String(Base64.decode(newServer.username, Base64.DEFAULT)),
                String(Base64.decode(newServer.password, Base64.DEFAULT)))
        streaming = newServer.streaming
        encStreaming = newServer.encStreaming
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .edit()
                .putString("chinachuAddress", newServer.chinachuAddress)
                .commit()
    }

    fun getEncodeSetting(type: Spinner,
                         containerFormat: EditText, videoCodec: EditText, audioCodec: EditText,
                         videoBitrate: EditText, videoBitrateFormat: Spinner,
                         audioBitrate: EditText, audioBitrateFormat: Spinner,
                         videoSize: EditText, frame: EditText): Encode {
        var vb = ""
        var ab = ""
        if (videoBitrate.text.toString().isNotEmpty()) {
            var videoBit = videoBitrate.text.toString().toInt()
            videoBit *= if (videoBitrateFormat.selectedItemPosition == 0) 1000 else 1000000
            vb = videoBit.toString()
        }

        if (audioBitrate.text.toString().isNotEmpty()) {
            var audioBit = audioBitrate.text.toString().toInt()
            audioBit *= if (audioBitrateFormat.selectedItemPosition == 0) 1000 else 1000000
            ab = audioBit.toString()
        }

        return Encode(
                type.selectedItem as String,
                containerFormat.text.toString(),
                videoCodec.text.toString(),
                audioCodec.text.toString(),
                vb,
                ab,
                videoSize.text.toString(),
                frame.text.toString())
    }

}