package com.tao.chinachuclient.viewmodel

import android.app.Application
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tao.chinachuclient.App

abstract class BaseActivitySettingViewModel(app: Application) : AndroidViewModel(app) {

    protected val app by lazy { getApplication<App>() }

    val chinachuAddress = MutableLiveData("")
    val username = MutableLiveData("")
    val password = MutableLiveData("")
    val encodeTypePosition = MutableLiveData(0)
    val encodeContainerFormat = MutableLiveData("")
    val encodeVideoCodec = MutableLiveData("")
    val encodeAudioCodec = MutableLiveData("")
    val encodeVideoBitrate = MutableLiveData("")
    val encodeVideoBitrateUnitPosition = MutableLiveData(0)
    val encodeAudioBitrate = MutableLiveData("")
    val encodeAudioBitrateUnitPosition = MutableLiveData(0)
    val encodeVideoSize = MutableLiveData("")
    val encodeFrame = MutableLiveData("")

    fun closeKeyboard(v: View) {
        val inputMethodManager =
            app.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    abstract fun onOk()

}
