package com.tao.chinachuclient.viewmodel

import android.app.Application
import android.net.Uri
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App
import com.tao.chinachuclient.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Program
import java.util.*

class ChannelScheduleActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _onToast = LiveEvent<Int>()
    val onToast: LiveData<Int> = _onToast

    private val _isShowLoading = MutableLiveData(View.VISIBLE)
    val isShowLoading: LiveData<Int> = _isShowLoading

    fun getChannelSpinnerItems(): List<String> =
        app.currentServer.channelNames.split(Regex("\\s*,\\s*"))

    private val _programList = LiveEvent<Array<Program>>()
    val programList: LiveData<Array<Program>> = _programList

    private val _programListSelection = LiveEvent<Int>()
    val programListSelection: LiveData<Int> = _programListSelection

    val channelSpinnerSelectedItemPosition = LiveEvent<Int>()

    private fun getSelectingChannelId() =
        app.currentServer.channelIds.split(Regex("\\s*,\\s*"))[channelSpinnerSelectedItemPosition.value!!]

    fun onChangeChannelSpinner() {
        _programList.value = arrayOf()
        _isShowLoading.value = View.VISIBLE

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { app.chinachu.getChannelSchedule(getSelectingChannelId()) }.getOrNull()
            }
            _isShowLoading.value = View.GONE

            if (result == null) {
                _onToast.value = R.string.error_get_schedule
                return@launch
            }

            Arrays.sort(result) { o1: Program, o2: Program ->
                when {
                    o1.start > o2.start -> 1
                    o1.start < o2.start -> -1
                    else -> 0
                }
            }
            result.let { _programList.value = it }

            val now = Date().time
            result.forEachIndexed { index, program ->
                if (program.start < now && program.end > now) {
                    _programListSelection.value = index
                    return@forEachIndexed
                }
            }
        }
    }

    fun getBroadcastingProgramTitle(): String {
        val now = Date().time
        val program = programList.value?.find {
            it.start < now && it.end > now
        }
        return program?.title ?: ""
    }

    fun getCurrentChannelStreamingUri(): Uri =
        Uri.parse(app.chinachu.getNonEncLiveMovieURL(getSelectingChannelId()))

    fun getCurrentChannelEncodeStreamingUri(): Uri = app.currentServer.encode.let {
        val type = it.type
        val params = arrayOf(
            it.containerFormat,
            it.videoCodec,
            it.audioCodec,
            it.videoBitrate,
            it.audioBitrate,
            it.videoSize,
            it.frame
        )
        Uri.parse(app.chinachu.getEncLiveMovieURL(getSelectingChannelId(), type, params))
    }
}
