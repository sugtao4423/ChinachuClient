package com.tao.chinachuclient.ui.preference

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.App
import com.tao.chinachuclient.R
import kotlinx.coroutines.launch

class PreferenceFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<App>()

    private val _showCheckSettingDialog = LiveEvent<Unit>()
    val showCheckSettingDialog: LiveData<Unit> = _showCheckSettingDialog

    private val _showDeleteServerDialog = LiveEvent<List<String>>()
    val showDeleteServerDialog: LiveData<List<String>> = _showDeleteServerDialog

    private val _onToast = LiveEvent<Int>()
    val onToast: LiveData<Int> = _onToast

    data class InitData(
        val isStreaming: Boolean,
        val isEncStreaming: Boolean,
        val isOldCategoryColor: Boolean
    )

    fun getInitData() = InitData(
        app.currentServer.streaming,
        app.currentServer.encStreaming,
        app.currentServer.oldCategoryColor
    )

    fun updateStreaming(newValue: Any) {
        newValue as Boolean
        viewModelScope.launch {
            app.serverRepository.updateStreaming(newValue, app.currentServer.chinachuAddress)
        }
    }

    fun updateEncStreaming(newValue: Any) {
        newValue as Boolean
        viewModelScope.launch {
            app.serverRepository.updateEncStreaming(newValue, app.currentServer.chinachuAddress)
        }
        if (newValue) {
            _showCheckSettingDialog.value = Unit
        }
    }

    fun updateOldCategoryColor(newValue: Any) {
        newValue as Boolean
        viewModelScope.launch {
            app.serverRepository.updateOldCategoryColor(newValue, app.currentServer.chinachuAddress)
        }
    }

    fun showDeleteServerDialog() {
        viewModelScope.launch {
            val addresses = app.serverRepository.getAll().map { it.chinachuAddress }
            _showDeleteServerDialog.value = addresses
        }
    }

    fun deleteServer(chinachuAddress: String) {
        viewModelScope.launch {
            app.serverRepository.delete(chinachuAddress)
            val servers = app.serverRepository.getAll()
            if (servers.isEmpty()) {
                app.prefRepository.clear()
            } else {
                app.changeCurrentServer(servers.first())
            }
            _onToast.value = R.string.deleted
        }
    }

    fun onDestroyPreferenceActivity() {
        viewModelScope.launch {
            app.reloadCurrentServer()
        }
    }
}
