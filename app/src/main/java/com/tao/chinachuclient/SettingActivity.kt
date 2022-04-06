package com.tao.chinachuclient

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.databinding.ActivitySettingBinding
import com.tao.chinachuclient.entity.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var app: App
    private lateinit var oldServer: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = applicationContext as App
        CoroutineScope(Dispatchers.Main).launch {
            app.reloadCurrentServer()
            oldServer = app.currentServer

            AlertDialog.Builder(this@SettingActivity)
                .setTitle(R.string.change_settings)
                .setMessage(getString(R.string.change_current_server_settings) + "\n\n" + getString(R.string.current_server) + "\n${oldServer.chinachuAddress}")
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    finish()
                }
                .show()
            initView()
        }
    }

    private fun initView() {
        binding.chinachuAddress.setText(oldServer.chinachuAddress)
        binding.username.setText(String(Base64.decode(oldServer.username, Base64.DEFAULT)))
        binding.password.setText(String(Base64.decode(oldServer.password, Base64.DEFAULT)))

        binding.encSettingTypeSpinner.setSelection(when (oldServer.encode.type) {
            "mp4" -> 0
            "m2ts" -> 1
            "webm" -> 2
            else -> 0
        })
        binding.encSettingContainerEdit.setText(oldServer.encode.containerFormat)
        binding.encSettingVideoCodecEdit.setText(oldServer.encode.videoCodec)
        binding.encSettingAudioCodecEdit.setText(oldServer.encode.audioCodec)

        val videoBit = oldServer.encode.videoBitrate.let {
            if (it.isEmpty()) 0 else it.toInt()
        }
        when {
            (videoBit / 1000 / 1000) != 0 -> {
                binding.encSettingVideoBitrateSpinner.setSelection(1)
                binding.encSettingVideoBitrate.setText((videoBit / 1000 / 1000).toString())
            }
            (videoBit / 1000) != 0 -> {
                binding.encSettingVideoBitrateSpinner.setSelection(0)
                binding.encSettingVideoBitrate.setText((videoBit / 1000).toString())
            }
            else -> binding.encSettingVideoBitrate.setText("")
        }

        val audioBit = oldServer.encode.audioBitrate.let {
            if (it.isEmpty()) 0 else it.toInt()
        }
        when {
            (audioBit / 1000 / 1000) != 0 -> {
                binding.encSettingAudioBitrateSpinner.setSelection(1)
                binding.encSettingAudioBitrate.setText((audioBit / 1000 / 1000).toString())
            }
            (audioBit / 1000) != 0 -> {
                binding.encSettingAudioBitrateSpinner.setSelection(0)
                binding.encSettingAudioBitrate.setText((audioBit / 1000).toString())
            }
            else -> binding.encSettingAudioBitrate.setText("")
        }

        binding.encSettingVideoSize.setText(oldServer.encode.videoSize)
        binding.encSettingFrame.setText(oldServer.encode.frame)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") v: View) {
        val rawChinachuAddress = binding.chinachuAddress.text.toString()
        if (!(rawChinachuAddress.startsWith("http://") || rawChinachuAddress.startsWith("https://"))) {
            Toast.makeText(this, R.string.wrong_server_address, Toast.LENGTH_SHORT).show()
            return
        }

        val newEncode = app.getEncodeSetting(
                binding.encSettingTypeSpinner,
                binding.encSettingContainerEdit,
                binding.encSettingVideoCodecEdit,
                binding.encSettingAudioCodecEdit,
                binding.encSettingVideoBitrate,
                binding.encSettingVideoBitrateSpinner,
                binding.encSettingAudioBitrate,
                binding.encSettingAudioBitrateSpinner,
                binding.encSettingVideoSize,
                binding.encSettingFrame
        )

        val newServer = Server(rawChinachuAddress,
                Base64.encodeToString(binding.username.text.toString().toByteArray(), Base64.DEFAULT),
                Base64.encodeToString(binding.password.text.toString().toByteArray(), Base64.DEFAULT),
                oldServer.streaming,
                oldServer.encStreaming,
                newEncode,
                "", "",
                oldServer.oldCategoryColor)

        CoroutineScope(Dispatchers.Main).launch {
            app.serverRepository.update(newServer)
            app.changeCurrentServer(newServer)
            finish()
        }
    }

    fun background(v: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}