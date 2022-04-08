package com.tao.chinachuclient.viewmodel

import android.app.Application
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tao.chinachuclient.App

abstract class BaseActivitySettingViewModel(app: Application) : AndroidViewModel(app) {

    private val app by lazy { getApplication<App>() }

    val chinachuAddress = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val encodeTypePosition = MutableLiveData<Int>()
    val encodeContainerFormat = MutableLiveData<String>()
    val encodeVideoCodec = MutableLiveData<String>()
    val encodeAudioCodec = MutableLiveData<String>()
    val encodeVideoBitrate = MutableLiveData<String>()
    val encodeVideoBitrateUnitPosition = MutableLiveData<Int>()
    val encodeAudioBitrate = MutableLiveData<String>()
    val encodeAudioBitrateUnitPosition = MutableLiveData<Int>()
    val encodeVideoSize = MutableLiveData<String>()
    val encodeFrame = MutableLiveData<String>()

    fun closeKeyboard(v: View) {
        val inputMethodManager =
            app.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    abstract fun onOk()

}
