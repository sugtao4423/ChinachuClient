package com.tao.chinachuclient

import android.app.Application
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.widget.EditText
import android.widget.Spinner
import com.tao.chinachuclient.db.ServerRoomDatabase
import com.tao.chinachuclient.entity.Encode
import com.tao.chinachuclient.entity.Server
import com.tao.chinachuclient.model.PrefRepository
import com.tao.chinachuclient.model.ServerRepository
import kotlinx.coroutines.runBlocking
import sugtao4423.library.chinachu4j.Chinachu4j

class App : Application() {

    private val serverDatabase by lazy { ServerRoomDatabase.getDatabase(this) }
    val serverRepository by lazy { ServerRepository(serverDatabase.serverDao()) }

    private val prefRepository by lazy { PrefRepository(this) }

    var chinachuInitialized = false
        private set
    lateinit var chinachu: Chinachu4j
        private set
    lateinit var currentServer: Server
        private set
    var streaming: Boolean = false
        private set
    var encStreaming: Boolean = false
        private set
    var reloadList: Boolean = false

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            reloadCurrentServer()
        }
    }

    suspend fun reloadCurrentServer() {
        val currentChinachuAddress = prefRepository.getChinachuAddress()
        serverRepository.findByAddress(currentChinachuAddress)?.let {
            currentServer = it
            val username = String(Base64.decode(it.username, Base64.DEFAULT))
            val password = String(Base64.decode(it.password, Base64.DEFAULT))
            chinachu = Chinachu4j(it.chinachuAddress, username, password)
            streaming = it.streaming
            encStreaming = it.encStreaming
            chinachuInitialized = true
        }
    }

    suspend fun changeCurrentServer(newServer: Server) {
        prefRepository.putChinachuAddress(newServer.chinachuAddress)
        reloadCurrentServer()
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

    @Suppress("DEPRECATION")
    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

}