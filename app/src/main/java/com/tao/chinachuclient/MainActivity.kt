package com.tao.chinachuclient

import Chinachu4j.Chinachu4j
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView


class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var pref: SharedPreferences
    private lateinit var chinachu: Chinachu4j
    private lateinit var appClass: ApplicationClass
    private lateinit var chinachuAddress: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var mainList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainList = ListView(this)
        setContentView(mainList)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        appClass = applicationContext as ApplicationClass

        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            val versionCode = pi.versionCode
            if (pref.getInt("versionCode", 0) < versionCode) {
                DBUtils(this).close()
                pref.edit().putInt("versionCode", versionCode).commit()
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

        chinachuAddress = pref.getString("chinachuAddress", "") ?: ""
        username = pref.getString("username", "") ?: ""
        password = pref.getString("password", "") ?: ""
        if (chinachuAddress == "" || username == "" || password == "") {
            startActivity(Intent(this, AddServer::class.java).apply {
                putExtra("startMain", true)
            })
            finish()
            return
        }
        username = String(Base64.decode(username, Base64.DEFAULT))
        password = String(Base64.decode(password, Base64.DEFAULT))

        val listItem = resources.getStringArray(R.array.main_list_names)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem)
        mainList.adapter = adapter
        mainList.onItemClickListener = this

        chinachu = Chinachu4j(chinachuAddress, username, password)

        appClass.chinachu = chinachu
        appClass.streaming = pref.getBoolean("streaming", false)
        appClass.encStreaming = pref.getBoolean("encStreaming", false)
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
            val dbUtils = DBUtils(this)
            val address = arrayListOf<String>()
            val servers = dbUtils.getServers()
            servers.map {
                address.add(it.chinachuAddress)
            }

            val settingNow = address.indexOf(pref.getString("chinachuAddress", "") ?: "")
            AlertDialog.Builder(this)
                    .setTitle(R.string.select_server)
                    .setSingleChoiceItems(address.toTypedArray(), settingNow) { dialog, which ->
                        val selectedAddress = address[which]
                        val server = dbUtils.getServerFromAddress(selectedAddress)
                        dbUtils.serverPutPref(server)

                        chinachu = Chinachu4j(server.chinachuAddress,
                                String(Base64.decode(server.username, Base64.DEFAULT)),
                                String(Base64.decode(server.password, Base64.DEFAULT)))
                        appClass.chinachu = chinachu
                        appClass.streaming = server.streaming
                        appClass.encStreaming = server.encStreaming

                        dbUtils.close()
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.cancel, null)
                    .show()
        } else if (item?.itemId == Menu.FIRST + 1) {
            startActivity(Intent(this, Preference::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}
