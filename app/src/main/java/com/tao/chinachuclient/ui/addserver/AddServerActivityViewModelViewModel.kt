package com.tao.chinachuclient.ui.addserver

import android.app.Application
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.R
import com.tao.chinachuclient.entity.Server
import com.tao.chinachuclient.model.EncodeUtil
import com.tao.chinachuclient.ui.BaseActivitySettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Chinachu4j
import sugtao4423.library.chinachu4j.Program

class AddServerActivityViewModelViewModel(app: Application) : BaseActivitySettingViewModel(app) {

    var isStartMainActivity = false

    private val _onStartMainActivity = LiveEvent<Unit>()
    val onStartMainActivity: LiveData<Unit> = _onStartMainActivity

    private val _onToast = LiveEvent<Int>()
    val onToast: LiveData<Int> = _onToast

    private val _toggleGettingChannelDialog = LiveEvent<Unit>()
    val toggleGettingChannelDialog: LiveData<Unit> = _toggleGettingChannelDialog

    private val _onFinish = LiveEvent<Unit>()
    val onFinish: LiveData<Unit> = _onFinish

    private fun isValidChinachuAddress(): Boolean {
        return chinachuAddress.value!!.startsWith("http://") || chinachuAddress.value!!.startsWith("https://")
    }

    override fun onOk() {
        if (!isValidChinachuAddress()) {
            _onToast.value = R.string.wrong_server_address
            return
        }

        val serverRepo = app.serverRepository
        viewModelScope.launch {
            val serverExists = serverRepo.isExists(chinachuAddress.value!!)
            if (serverExists) {
                _onToast.value = R.string.already_register
                return@launch
            }

            _toggleGettingChannelDialog.value = Unit
            val chinachu = Chinachu4j(chinachuAddress.value!!, username.value!!, password.value!!)
            val schedule = withContext(Dispatchers.IO) {
                runCatching { chinachu.getAllSchedule() }.getOrNull()
            }
            _toggleGettingChannelDialog.value = Unit
            if (schedule == null) {
                _onToast.value = R.string.error_get_channel_list
                return@launch
            }

            val server = addServer(schedule)

            if (isStartMainActivity) {
                app.changeCurrentServer(server)
                _onStartMainActivity.value = Unit
            }
            _onFinish.value = Unit
        }
    }

    private suspend fun addServer(schedule: Array<Program>): Server {
        val channelIds = arrayListOf<String>()
        val channelNames = arrayListOf<String>()
        schedule.forEach {
            if (!channelIds.contains(it.channel.id)) {
                channelIds.add(it.channel.id)
                channelNames.add(it.channel.name)
            }
        }

        val encode = EncodeUtil.getEncodeSetting(
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
        val server = Server(
            chinachuAddress.value!!,
            Base64.encodeToString(username.value!!.toByteArray(), Base64.DEFAULT),
            Base64.encodeToString(password.value!!.toByteArray(), Base64.DEFAULT),
            streaming = false,
            encStreaming = false,
            encode,
            channelIds.joinToString(),
            channelNames.joinToString(),
            oldCategoryColor = false
        )
        app.serverRepository.insert(server)
        return server
    }
}
