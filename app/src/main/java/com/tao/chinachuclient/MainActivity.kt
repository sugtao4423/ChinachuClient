package com.tao.chinachuclient

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var appClass: ApplicationClass
    private lateinit var dbUtils: DBUtils
    private lateinit var currentServer: Server
    private lateinit var mainList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainList = ListView(this)
        setContentView(mainList)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        appClass = applicationContext as ApplicationClass
        dbUtils = DBUtils(this)

        val chinachuAddress = pref.getString("chinachuAddress", "") ?: ""
        val serverExists = dbUtils.serverExists(chinachuAddress)
        if (chinachuAddress == "" || !serverExists) {
            startActivity(Intent(this, AddServer::class.java).apply {
                putExtra("startMain", true)
            })
            finish()
            return
        }

        currentServer = dbUtils.getServerFromAddress(chinachuAddress)
        appClass.changeCurrentServer(currentServer)

        val listItem = resources.getStringArray(R.array.main_list_names)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem)
        mainList.adapter = adapter
        mainList.onItemClickListener = this
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == Menu.FIRST) {
            val address = arrayListOf<String>()
            val servers = dbUtils.getServers()
            servers.map {
                address.add(it.chinachuAddress)
            }

            val settingNow = address.indexOf(currentServer.chinachuAddress)
            AlertDialog.Builder(this)
                    .setTitle(R.string.select_server)
                    .setSingleChoiceItems(address.toTypedArray(), settingNow) { dialog, which ->
                        val selectedAddress = address[which]
                        currentServer = dbUtils.getServerFromAddress(selectedAddress)
                        appClass.changeCurrentServer(currentServer)
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.cancel, null)
                    .show()
        } else if (item?.itemId == Menu.FIRST + 1) {
            startActivity(Intent(this, Preference::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        dbUtils.close()
    }

}
