package com.tao.chinachuclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Chinachu4j
import sugtao4423.support.progressdialog.ProgressDialog

class AddServer : AppCompatActivity() {

    private lateinit var chinachuAddress: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private var startMain: Boolean = false

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

        startMain = intent.getBooleanExtra("startMain", false)

        chinachuAddress = findViewById(R.id.chinachuAddress)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        type = findViewById(R.id.encSettingTypeSpinner)
        containerFormat = findViewById(R.id.encSettingContainerEdit)
        videoCodec = findViewById(R.id.encSettingVideoCodecEdit)
        audioCodec = findViewById(R.id.encSettingAudioCodecEdit)

        videoBitrate = findViewById(R.id.encSettingVideoBitrate)
        videoBitrateFormat = findViewById(R.id.encSettingVideoBitrateSpinner)
        audioBitrate = findViewById(R.id.encSettingAudioBitrate)
        audioBitrateFormat = findViewById(R.id.encSettingAudioBitrateSpinner)
        videoSize = findViewById(R.id.encSettingVideoSize)
        frame = findViewById(R.id.encSettingFrame)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") v: View) {
        val address = chinachuAddress.text.toString()
        if (!(address.startsWith("http://") || address.startsWith("https://"))) {
            Toast.makeText(this, R.string.wrong_server_address, Toast.LENGTH_SHORT).show()
            return
        }

        val dbUtils = DBUtils(this)
        if (dbUtils.serverExists(address)) {
            Toast.makeText(this, R.string.already_register, Toast.LENGTH_SHORT).show()
            dbUtils.close()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = ProgressDialog(this@AddServer).apply {
                setMessage(getString(R.string.getting_channel_list))
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                show()
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    val chinachu = Chinachu4j(address, username.text.toString(), password.text.toString())
                    chinachu.getAllSchedule()
                } catch (e: Exception) {
                    null
                }
            }
            progressDialog.dismiss()
            if (result == null) {
                Toast.makeText(this@AddServer, R.string.error_get_channel_list, Toast.LENGTH_SHORT).show()
                return@launch
            }

            val channelIds = arrayListOf<String>()
            val channelNames = arrayListOf<String>()
            result.forEach {
                if (!channelIds.contains(it.channel.id)) {
                    channelIds.add(it.channel.id)
                    channelNames.add(it.channel.name)
                }
            }

            val encode = (applicationContext as App).getEncodeSetting(
                    type, containerFormat, videoCodec, audioCodec,
                    videoBitrate, videoBitrateFormat, audioBitrate, audioBitrateFormat, videoSize, frame
            )
            val server = Server(
                    address,
                    Base64.encodeToString(username.text.toString().toByteArray(), Base64.DEFAULT),
                    Base64.encodeToString(password.text.toString().toByteArray(), Base64.DEFAULT),
                    false, false, encode, channelIds.joinToString(), channelNames.joinToString(), false
            )
            dbUtils.insertServer(server)
            dbUtils.close()

            if (startMain) {
                (applicationContext as App).changeCurrentServer(server)
                startActivity(Intent(this@AddServer, MainActivity::class.java))
            }
            finish()
        }
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