package com.tao.chinachuclient.ui

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App

abstract class BaseActivityProgramViewModel(app: Application) : AndroidViewModel(app) {

    protected val app by lazy { getApplication<App>() }

    val isShowLoading = MutableLiveData(View.VISIBLE)
    val isShowSwipeRefresh = MutableLiveData(false)

    val onToast = LiveEvent<String>()
    val actionBarTitle = MutableLiveData<String>()
    val programList = MutableLiveData<Array<*>>()

    abstract fun refreshActionBarTitle(count: Int = -1)

    abstract fun loadData(isRefresh: Boolean)

    val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        loadData(true)
    }

}
