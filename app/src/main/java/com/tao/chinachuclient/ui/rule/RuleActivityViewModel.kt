package com.tao.chinachuclient.ui.rule

import android.app.Application
import android.view.View
import androidx.lifecycle.viewModelScope
import com.tao.chinachuclient.R
import com.tao.chinachuclient.ui.BaseActivityProgramViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RuleActivityViewModel(app: Application) : BaseActivityProgramViewModel(app) {

    init {
        refreshActionBarTitle()
        loadData(false)
    }

    override fun refreshActionBarTitle(count: Int) {
        var title = app.applicationContext.resources.getString(R.string.rule)
        if (count >= 0) {
            title += " ($count)"
        }
        actionBarTitle.value = title
    }

    override fun loadData(isRefresh: Boolean) {
        programList.value = arrayOf<Any>()

        isShowLoading.value = if (isRefresh) View.GONE else View.VISIBLE
        isShowSwipeRefresh.value = isRefresh

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { app.chinachu.getRules() }.getOrNull()
            }
            isShowLoading.value = View.GONE
            isShowSwipeRefresh.value = false

            if (result == null) {
                onToast.value = app.resources.getString(R.string.error_get_rule)
                return@launch
            }

            programList.value = result
            refreshActionBarTitle(result.size)
        }
    }
}
