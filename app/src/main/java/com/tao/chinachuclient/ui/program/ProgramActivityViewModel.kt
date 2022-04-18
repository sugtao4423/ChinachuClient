package com.tao.chinachuclient.ui.program

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.R
import com.tao.chinachuclient.ui.BaseActivityProgramViewModel
import com.tao.chinachuclient.ui.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.library.chinachu4j.Program
import sugtao4423.library.chinachu4j.Recorded
import sugtao4423.library.chinachu4j.Reserve
import java.text.Normalizer
import java.util.*

class ProgramActivityViewModel(app: Application) : BaseActivityProgramViewModel(app) {

    var listType = -1
        set(value) {
            if (field == value) return
            field = value
            refreshActionBarTitle()
            loadData(false)
        }
    var query: String? = null

    private val _filteredProgramList = MutableLiveData<Array<*>>()
    val filteredProgramList: LiveData<Array<*>> = _filteredProgramList

    private val _toggleLoadingProgressDialog = LiveEvent<Unit>()
    val toggleLoadingProgressDialog: LiveData<Unit> = _toggleLoadingProgressDialog

    private val _onRecordedListCleanUpSuccess = LiveEvent<Unit>()
    val onRecordedListCleanUpSuccess: LiveData<Unit> = _onRecordedListCleanUpSuccess

    override fun refreshActionBarTitle(count: Int) {
        val titleRes = when (listType) {
            Type.RESERVES -> R.string.reserved
            Type.RECORDING -> R.string.recording
            Type.RECORDED -> R.string.recorded
            Type.SEARCH_PROGRAM -> R.string.search_result
            else -> throw UnsupportedOperationException()
        }
        var title = app.resources.getString(titleRes)
        if (count >= 0) {
            title += " ($count)"
        }
        actionBarTitle.value = title
    }

    override fun loadData(isRefresh: Boolean) {
        programList.value = arrayOf<Any>()
        _filteredProgramList.value = arrayOf<Any>()

        isShowLoading.value = if (isRefresh) View.GONE else View.VISIBLE
        isRefreshing.set(isRefresh)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { getData() }
            isShowLoading.value = View.GONE
            isRefreshing.set(false)

            if (result == null) {
                onToast.value = app.resources.getString(R.string.error_get_schedule)
                return@launch
            }

            result.let {
                programList.value = it
                _filteredProgramList.value = it
            }
            refreshActionBarTitle(result.size)
        }
    }

    private fun getData(): Array<*>? {
        return runCatching {
            when (listType) {
                Type.RESERVES -> app.chinachu.getReserves()
                Type.RECORDING -> app.chinachu.getRecording()
                Type.RECORDED -> app.chinachu.getRecorded().reversedArray()
                Type.SEARCH_PROGRAM -> {
                    val search = app.chinachu.searchProgram(query ?: "")
                    Arrays.sort(search) { o1: Program, o2: Program ->
                        when {
                            o1.start > o2.start -> 1
                            o1.start < o2.start -> -1
                            else -> 0
                        }
                    }
                    search
                }
                else -> null
            }
        }.getOrNull()
    }

    fun onSearchQueryChanged(query: String) {
        if (query.isEmpty()) {
            _filteredProgramList.value = programList.value
            return
        }

        _filteredProgramList.value = programList.value!!.filter {
            val item = when (listType) {
                Type.RESERVES -> (it as Reserve).program
                Type.RECORDED -> (it as Recorded).program
                else -> (it as Program)
            }
            val itemTitle = Normalizer.normalize(item.fullTitle, Normalizer.Form.NFKC).lowercase()
            val searchText = Normalizer.normalize(query, Normalizer.Form.NFKC).lowercase()
            itemTitle.contains(searchText)
        }.toTypedArray()
    }

    fun onRecordedListCleanUp() {
        viewModelScope.launch {
            _toggleLoadingProgressDialog.value = Unit
            val result = withContext(Dispatchers.IO) {
                runCatching { app.chinachu.recordedCleanUp() }.getOrNull()
            }
            _toggleLoadingProgressDialog.value = Unit

            if (result == null) {
                onToast.value = app.resources.getString(R.string.error_access)
                return@launch
            }
            if (!result.result) {
                onToast.value = result.message
                return@launch
            }
            _onRecordedListCleanUpSuccess.value = Unit
        }
    }
}
