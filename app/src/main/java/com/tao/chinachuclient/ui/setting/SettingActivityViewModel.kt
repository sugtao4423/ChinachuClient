package com.tao.chinachuclient.ui.setting

import android.app.Application
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import com.tao.chinachuclient.R
import com.tao.chinachuclient.entity.Server
import com.tao.chinachuclient.model.EncodeUtil
import com.tao.chinachuclient.ui.BaseActivitySettingViewModel
import kotlinx.coroutines.launch

class SettingActivityViewModel(application: Application) :
    BaseActivitySettingViewModel(application) {

    val currentServerAddress = app.currentServer.chinachuAddress

    val onShowConfirmDialog = LiveEvent<Unit>(LiveEventConfig.PreferFirstObserver)

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    init {
        initServerSettings(app.currentServer)
        onShowConfirmDialog.value = Unit
    }

    private fun initServerSettings(server: Server) {
        chinachuAddress.value = server.chinachuAddress
        disableChinachuAddress.value = true
        username.value = String(Base64.decode(server.username, Base64.DEFAULT))
        password.value = String(Base64.decode(server.password, Base64.DEFAULT))

        val typeStringArray =
            app.applicationContext.resources.getStringArray(R.array.enc_setting_typeSpinner_item)
        encodeTypePosition.value = typeStringArray.indexOf(server.encode.type)

        encodeContainerFormat.value = server.encode.containerFormat
        encodeVideoCodec.value = server.encode.videoCodec
        encodeAudioCodec.value = server.encode.audioCodec
        setBitrateAndUnit(
            server.encode.videoBitrate,
            encodeVideoBitrate,
            encodeVideoBitrateUnitPosition
        )
        setBitrateAndUnit(
            server.encode.audioBitrate,
            encodeAudioBitrate,
            encodeAudioBitrateUnitPosition
        )
        encodeVideoSize.value = server.encode.videoSize
        encodeFrame.value = server.encode.frame
    }

    private fun setBitrateAndUnit(
        bitrate: String,
        bitrateView: MutableLiveData<String>,
        bitrateUnitViewPos: MutableLiveData<Int>
    ) {
        val bit = bitrate.let { if (it.isEmpty()) 0 else it.toInt() }
        when {
            (bit / 1000 / 1000) != 0 -> {
                bitrateView.value = (bit / 1000 / 1000).toString()
                bitrateUnitViewPos.value = 1
            }
            (bit / 1000) != 0 -> {
                bitrateView.value = (bit / 1000).toString()
                bitrateUnitViewPos.value = 0
            }
            else -> bitrateView.value = ""
        }
    }

    override fun onOk() {
        val newEncode = EncodeUtil.getEncodeSetting(
            app.applicationContext,
            encodeTypePosition.value!!,
            encodeContainerFormat.value!!,
            encodeVideoCodec.value!!,
            encodeAudioCodec.value!!,
            encodeVideoBitrate.value!!,
            encodeVideoBitrateUnitPosition.value!!,
            encodeAudioBitrate.value!!,
            encodeAudioBitrateUnitPosition.value!!,
            encodeVideoSize.value!!,
            encodeFrame.value!!
        )
        val newServer = Server(
            chinachuAddress.value!!,
            Base64.encodeToString(username.value!!.toByteArray(), Base64.DEFAULT),
            Base64.encodeToString(password.value!!.toByteArray(), Base64.DEFAULT),
            app.currentServer.streaming,
            app.currentServer.encStreaming,
            newEncode,
            app.currentServer.channelIds,
            app.currentServer.channelNames,
            app.currentServer.oldCategoryColor
        )

        viewModelScope.launch {
            app.serverRepository.update(newServer)
            app.changeCurrentServer(newServer)
            _onFinish.value = Unit
        }
    }
}
