package com.tao.chinachuclient.ui

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App

abstract class BaseActivityProgramViewModel(app: Application) : AndroidViewModel(app) {

    protected val app by lazy { getApplication<App>() }

    val isShowLoading = MutableLiveData(View.VISIBLE)
    val isRefreshing = ObservableField(false)

    val onToast = LiveEvent<String>()
    val actionBarTitle = MutableLiveData<String>()
    val programList = MutableLiveData<Array<*>>()

    abstract fun refreshActionBarTitle(count: Int = -1)

    abstract fun loadData(isRefresh: Boolean)

    fun onRefresh() = loadData(true)

}
