package com.tao.chinachuclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tao.chinachuclient.databinding.ActivitySettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Chinachu4j
import sugtao4423.support.progressdialog.ProgressDialog

class AddServer : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var startMain: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        startMain = intent.getBooleanExtra("startMain", false)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") v: View) {
        val address = binding.chinachuAddress.text.toString()
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
                    val chinachu = Chinachu4j(address, binding.username.text.toString(), binding.password.text.toString())
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
            val server = Server(
                    address,
                    Base64.encodeToString(binding.username.text.toString().toByteArray(), Base64.DEFAULT),
                    Base64.encodeToString(binding.password.text.toString().toByteArray(), Base64.DEFAULT),
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}