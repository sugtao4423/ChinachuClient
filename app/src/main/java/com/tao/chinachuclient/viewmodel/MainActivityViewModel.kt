package com.tao.chinachuclient.viewmodel

import android.app.Application
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.tao.chinachuclient.*
import kotlinx.coroutines.launch

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    private val app = getApplication<App>()

    private val _onStartAddServerActivity = LiveEvent<Unit>()
    val onStartAddServerActivity: LiveData<Unit> = _onStartAddServerActivity

    init {
        if (!getApplication<App>().chinachuInitialized) {
            _onStartAddServerActivity.value = Unit
        }
    }

    private val _onStartActivityWoExtra = LiveEvent<Class<*>>()
    val onStartActivityWoExtra: LiveData<Class<*>> = _onStartActivityWoExtra

    private val _onStartProgramActivity = LiveEvent<Int>()
    val onStartProgramActivity: LiveData<Int> = _onStartProgramActivity

    val mainOnItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
        when (i) {
            Type.CHANNEL_SCHEDULE_ACTIVITY -> _onStartActivityWoExtra.value =
                ChannelScheduleActivity::class.java
            Type.RULE_ACTIVITY -> _onStartActivityWoExtra.value = RuleActivity::class.java
            else -> _onStartProgramActivity.value = i
        }
    }

    data class ChangeServerDialogData(
        val serverAddresses: List<String>,
        val currentServerPosition: Int
    )

    private val _changeServerDialogData = LiveEvent<ChangeServerDialogData>()
    val changeServerDialogData: LiveData<ChangeServerDialogData> = _changeServerDialogData

    fun changeServer(changedServerAddress: String) {
        viewModelScope.launch {
            val server = app.serverRepository.findByAddress(changedServerAddress)
            server?.let {
                app.changeCurrentServer(it)
            }
        }
    }

    fun onOptionsItemSelected(item: MenuItem) {
        if (item.itemId == Menu.FIRST) {
            viewModelScope.launch {
                val addresses = app.serverRepository.getAll().map { it.chinachuAddress }
                val currentServerIndex = addresses.indexOf(app.currentServer.chinachuAddress)
                _changeServerDialogData.value =
                    ChangeServerDialogData(addresses, currentServerIndex)
            }
        } else if (item.itemId == Menu.FIRST + 1) {
            _onStartActivityWoExtra.value = Preference::class.java
        }
    }

}