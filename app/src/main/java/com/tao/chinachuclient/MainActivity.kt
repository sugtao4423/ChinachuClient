package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var app: App
    private lateinit var mainList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainList = ListView(this)
        setContentView(mainList)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        app = applicationContext as App

        CoroutineScope(Dispatchers.Main).launch {
            val chinachuAddress = pref.getString("chinachuAddress", "") ?: ""
            val serverExists = withContext(Dispatchers.IO) { app.serverRepository.isExists(chinachuAddress) }
            if (chinachuAddress == "" || !serverExists) {
                startActivity(Intent(this@MainActivity, AddServer::class.java).apply {
                    putExtra("startMain", true)
                })
                finish()
                return@launch
            }

            withContext(Dispatchers.IO) { app.serverRepository.findByAddress(chinachuAddress) }?.let {
                app.changeCurrentServer(it)
            }

            val listItem = resources.getStringArray(R.array.main_list_names)
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, listItem)
            mainList.adapter = adapter
            mainList.onItemClickListener = this@MainActivity
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        startActivity(when (position) {
            Type.CHANNEL_SCHEDULE_ACTIVITY -> Intent(this, ChannelScheduleActivity::class.java)
            Type.RULE_ACTIVITY -> Intent(this, RuleActivity::class.java)
            else -> Intent(this, ProgramActivity::class.java).putExtra("type", position)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, Menu.FIRST, Menu.NONE, R.string.change_server)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        menu?.add(0, Menu.FIRST + 1, Menu.NONE, R.string.settings)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == Menu.FIRST) {
            CoroutineScope(Dispatchers.Main).launch {
                val servers = withContext(Dispatchers.IO) { app.serverRepository.getAll() }
                val address = servers.map { it.chinachuAddress }

                val currentServer = app.currentServer
                val settingNow = address.indexOf(currentServer.chinachuAddress)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.select_server)
                    .setSingleChoiceItems(address.toTypedArray(), settingNow) { dialog, which ->
                        CoroutineScope(Dispatchers.Main).launch {
                            val selectedAddress = address[which]
                            withContext(Dispatchers.IO) { app.serverRepository.findByAddress(selectedAddress) }?.let {
                                app.changeCurrentServer(it)
                            }
                            dialog.dismiss()
                        }
                    }
                    .setPositiveButton(R.string.cancel, null)
                    .show()
            }
        } else if (item.itemId == Menu.FIRST + 1) {
            startActivity(Intent(this, Preference::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
