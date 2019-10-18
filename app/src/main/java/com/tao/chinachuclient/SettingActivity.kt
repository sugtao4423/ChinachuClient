package com.tao.chinachuclient

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

class SettingActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var oldServer: Server

    private lateinit var chinachuAddress: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText

    private lateinit var type: Spinner
    private lateinit var videoBitrateFormat: Spinner
    private lateinit var audioBitrateFormat: Spinner
    private lateinit var containerFormat: EditText
    private lateinit var videoCodec: EditText
    private lateinit var audioCodec: EditText
    private lateinit var videoBitrate: EditText
    private lateinit var audioBitrate: EditText
    private lateinit var videoSize: EditText
    private lateinit var frame: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = applicationContext as App
        app.reloadCurrentServer()
        oldServer = app.currentServer

        AlertDialog.Builder(this)
                .setTitle(R.string.change_settings)
                .setMessage(getString(R.string.change_current_server_settings) + "\n\n" + getString(R.string.current_server) + "\n${oldServer.chinachuAddress}")
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    finish()
                }
                .show()

        chinachuAddress = findViewById(R.id.chinachuAddress)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        chinachuAddress.setText(oldServer.chinachuAddress)
        username.setText(String(Base64.decode(oldServer.username, Base64.DEFAULT)))
        password.setText(String(Base64.decode(oldServer.password, Base64.DEFAULT)))

        type = findViewById(R.id.enc_setting_type_spinner)
        containerFormat = findViewById(R.id.enc_setting_container_edit)
        videoCodec = findViewById(R.id.enc_setting_videoCodec_edit)
        audioCodec = findViewById(R.id.enc_setting_audioCodec_edit)

        videoBitrate = findViewById(R.id.enc_setting_videoBitrate)
        videoBitrateFormat = findViewById(R.id.enc_setting_video_bitrate_spinner)
        audioBitrate = findViewById(R.id.enc_setting_audioBitrate)
        audioBitrateFormat = findViewById(R.id.enc_setting_audio_bitrate_spinner)
        videoSize = findViewById(R.id.enc_setting_videoSize)
        frame = findViewById(R.id.enc_setting_frame)

        type.setSelection(when (oldServer.encode.type) {
            "mp4" -> 0
            "m2ts" -> 1
            "webm" -> 2
            else -> 0
        })
        containerFormat.setText(oldServer.encode.containerFormat)
        videoCodec.setText(oldServer.encode.videoCodec)
        audioCodec.setText(oldServer.encode.audioCodec)

        val videoBit = oldServer.encode.videoBitrate.let {
            if (it.isEmpty()) 0 else it.toInt()
        }
        when {
            (videoBit / 1000 / 1000) != 0 -> {
                videoBitrateFormat.setSelection(1)
                videoBitrate.setText((videoBit / 1000 / 1000).toString())
            }
            (videoBit / 1000) != 0 -> {
                videoBitrateFormat.setSelection(0)
                videoBitrate.setText((videoBit / 1000).toString())
            }
            else -> videoBitrate.setText("")
        }

        val audioBit = oldServer.encode.audioBitrate.let {
            if (it.isEmpty()) 0 else it.toInt()
        }
        when {
            (audioBit / 1000 / 1000) != 0 -> {
                audioBitrateFormat.setSelection(1)
                audioBitrate.setText((audioBit / 1000 / 1000).toString())
            }
            (audioBit / 1000) != 0 -> {
                audioBitrateFormat.setSelection(0)
                audioBitrate.setText((audioBit / 1000).toString())
            }
            else -> audioBitrate.setText("")
        }

        videoSize.setText(oldServer.encode.videoSize)
        frame.setText(oldServer.encode.frame)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") v: View) {
        val rawChinachuAddress = chinachuAddress.text.toString()
        if (!(rawChinachuAddress.startsWith("http://") || rawChinachuAddress.startsWith("https://"))) {
            Toast.makeText(this, R.string.wrong_server_address, Toast.LENGTH_SHORT).show()
            return
        }

        val newEncode = app.getEncodeSetting(
                type, containerFormat, videoCodec, audioCodec,
                videoBitrate, videoBitrateFormat, audioBitrate, audioBitrateFormat, videoSize, frame)

        val newServer = Server(rawChinachuAddress,
                Base64.encodeToString(username.text.toString().toByteArray(), Base64.DEFAULT),
                Base64.encodeToString(password.text.toString().toByteArray(), Base64.DEFAULT),
                oldServer.streaming,
                oldServer.encStreaming,
                newEncode,
                "", "",
                oldServer.oldCategoryColor)

        val dbUtils = DBUtils(this)
        dbUtils.updateServer(newServer, oldServer.chinachuAddress)
        dbUtils.close()
        app.changeCurrentServer(newServer)
        finish()
    }

    fun background(v: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}